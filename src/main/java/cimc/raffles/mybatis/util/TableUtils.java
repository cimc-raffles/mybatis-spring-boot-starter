package cimc.raffles.mybatis.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

public class TableUtils {

	public static Map<String, String> getMappedColumProperties(Class<?> clazz) {
		TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
		return getMappedColumProperties(tableInfo);
	}

	public static Map<String, String> getMappedColumProperties(TableInfo tableInfo) {
		Map<String, String> map = new HashMap<String, String>();
		String keyProperty = tableInfo.getKeyProperty();
		if (!StringUtils.isEmpty(keyProperty))
			map.put(keyProperty.toUpperCase(), tableInfo.getKeyColumn());
		tableInfo.getFieldList().forEach(x -> map.put(x.getProperty().toUpperCase(), x.getColumn()));
		return map;
	}

}
