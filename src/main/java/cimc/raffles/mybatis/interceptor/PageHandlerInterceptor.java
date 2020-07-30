package cimc.raffles.mybatis.interceptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.annotation.JsonProperty;

import cimc.raffles.mybatis.annotation.PageableDefault;
import cimc.raffles.mybatis.enumeration.Direction;
import cimc.raffles.mybatis.pagination.Pageable;
import cimc.raffles.mybatis.util.TableUtils;

public class PageHandlerInterceptor<T> implements HandlerMethodArgumentResolver {

	private final String KEYWORD_PAGE = "page";
	private final String KEYWORD_SIZE = "size";
	private final String KEYWORD_SORT = "sort";

	public PageHandlerInterceptor() {
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().isAssignableFrom(Pageable.class)
				&& parameter.hasParameterAnnotation(PageableDefault.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		String page = webRequest.getParameter(KEYWORD_PAGE);
		String size = webRequest.getParameter(KEYWORD_SIZE);

		String[] sorts = webRequest.getParameterValues(KEYWORD_SORT);

		PageableDefault pageable = parameter.getParameterAnnotation(PageableDefault.class);

		Pageable<T> model = new Pageable<T>();

		model.setCurrent(1L + (StringUtils.isEmpty(page) ? pageable.page() : Long.valueOf(page)));
		model.setSize(StringUtils.isEmpty(size) ? pageable.size() : Long.valueOf(size));

		if (null == sorts || sorts.length < 1)
			return model;

		Predicate<? super String> predicate = x -> StringUtils.trimAllWhitespace(x).toUpperCase()
				.endsWith(String.format(",%s", Direction.DESC.name()));

		String[] descs = Arrays.stream(sorts).filter(predicate).map(x -> StringUtils.commaDelimitedListToStringArray(x))
				.flatMap(Arrays::stream)
				.filter(x -> !StringUtils.trimAllWhitespace(x).toUpperCase().endsWith(Direction.DESC.name()))
				.toArray(String[]::new);

		String[] ascs = Arrays.stream(sorts).filter(predicate.negate())
				.map(x -> StringUtils.commaDelimitedListToStringArray(x)).flatMap(Arrays::stream)
				.filter(x -> !StringUtils.trimAllWhitespace(x).toUpperCase().endsWith(Direction.ASC.name()))
				.toArray(String[]::new);

		boolean hasDescs = null != descs && 0 < descs.length;
		boolean hasAscs = null != ascs && 0 < ascs.length;

		if (!hasDescs && !hasAscs)
			return model;

		if (hasDescs)
			model.addOrder(OrderItem.descs(descs));

		if (hasAscs)
			model.addOrder(OrderItem.ascs(ascs));

		ResolvableType[] resolvableTypes = ResolvableType.forMethodParameter(parameter).getGenerics();

		if (null == resolvableTypes || resolvableTypes.length < 1)
			return model;

		Class<?> clazz = resolvableTypes[0].resolve();

		if (null == clazz)
			return model;

		Map<String, String> columnPropertyMap = this.getColumnPropertyMap(clazz);

		if (null == columnPropertyMap || columnPropertyMap.isEmpty())
			return model;

		if (hasDescs) {
			this.removeOrder(model.getOrders(), item -> !item.isAsc());
			model.addOrder(Arrays.stream(descs).map(x -> {
				return OrderItem.desc(StringUtils.isEmpty(columnPropertyMap.get(x)) ? x : columnPropertyMap.get(x));
			}).collect(Collectors.toList()));
		}

		if (hasAscs) {
			this.removeOrder(model.getOrders(), OrderItem::isAsc);
			model.addOrder(Arrays.stream(ascs).map(x -> {
				return OrderItem.asc(StringUtils.isEmpty(columnPropertyMap.get(x)) ? x : columnPropertyMap.get(x));
			}).collect(Collectors.toList()));
		}

		return model;
	}

	private Map<String, String> getColumnPropertyMap(Class<?> clazz) {
		Map<String, String> result = new HashMap<>();

		TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
		boolean hasTableInfo = null != tableInfo;
		boolean isUnderCamel = false;
		if (hasTableInfo)
			isUnderCamel = tableInfo.isUnderCamel();
		else {
			// TODO:
			List<TableInfo> tableInfos = TableInfoHelper.getTableInfos();
			isUnderCamel = CollectionUtils.isEmpty(tableInfos) ? isUnderCamel : tableInfos.get(0).isUnderCamel();
		}

		Map<String, String> columnMap = hasTableInfo ? TableUtils.getMappedColumProperties(tableInfo) : null;

		Field[] fields = clazz.getDeclaredFields();
		for (Field x : fields) {
			String columnName = x.getName();

			if (!hasTableInfo) {
				if (isUnderCamel)
					result.put(columnName,
							com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(columnName));
			} else {

				String columnMappedName = columnMap.get(columnName.toUpperCase());

				if (StringUtils.isEmpty(columnMappedName)) {
					TableField tableFieldAnnotation = x.getAnnotation(TableField.class);
					if (null != tableFieldAnnotation && !StringUtils.isEmpty(tableFieldAnnotation.value()))
						columnMappedName = tableFieldAnnotation.value();
					else if (isUnderCamel)
						columnMappedName = com.baomidou.mybatisplus.core.toolkit.StringUtils
								.camelToUnderline(columnName);
				}

				if (!StringUtils.isEmpty(columnMappedName))
					result.put(columnName, columnMappedName);
			}

			// handle JsonProperty annotation column
			JsonProperty jsonAnnotation = x.getAnnotation(JsonProperty.class);

			if (null != jsonAnnotation) {
				if (!StringUtils.isEmpty(jsonAnnotation.defaultValue()))
					result.put(jsonAnnotation.defaultValue(), columnName);

				if (!StringUtils.isEmpty(jsonAnnotation.value()))
					result.put(jsonAnnotation.value(), columnName);
			}
		}
		;

		return result;
	}

	private void removeOrder(List<OrderItem> orders, Predicate<OrderItem> filter) {
		for (int i = orders.size() - 1; i >= 0; i--) {
			if (filter.test(orders.get(i))) {
				orders.remove(i);
			}
		}
	}

}
