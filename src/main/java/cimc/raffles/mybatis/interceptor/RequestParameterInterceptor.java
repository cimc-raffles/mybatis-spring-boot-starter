package cimc.raffles.mybatis.interceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cimc.raffles.mybatis.annotation.CustomRequestParam;


public class RequestParameterInterceptor implements HandlerMethodArgumentResolver 
{
	
	public RequestParameterInterceptor()
	{
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter)
	{
		return parameter.hasParameterAnnotation( CustomRequestParam.class);
	}

	@Override
	public Object resolveArgument( MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception 
	{

		final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
		
		Map<String,String[]> map = request.getParameterMap() ;
		
		Map<String,Object> result = new HashMap<>() ;
		
		for( Map.Entry<String,String[]> x :  map.entrySet())
		{
			String[] value = x.getValue() ;
			if( value  instanceof String[] && value.length ==1)
				result.put( x.getKey(), value[0]) ;
			else
				result.put( x.getKey(), value) ;
		}
		
		ObjectMapper mapper = 
				new ObjectMapper()
					.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) 
					.configure( DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
		;
		
		CustomRequestParam annotation = parameter.getParameterAnnotation( CustomRequestParam.class) ;
		DeserializationFeature[] disableFeatures = annotation.disable() ;
		DeserializationFeature[] enableFeatures = annotation.enable() ;
		
		if( null != disableFeatures && disableFeatures.length >0)
			Arrays.stream( disableFeatures ).forEach( x-> mapper.configure( x, false ));
		
		if( null != enableFeatures && enableFeatures.length >0)
			Arrays.stream( enableFeatures ).forEach( x-> mapper.configure( x, true ));
		
		final Object o = mapper.convertValue( result, parameter.getParameterType()) ;
		
		return o ;
	}

}