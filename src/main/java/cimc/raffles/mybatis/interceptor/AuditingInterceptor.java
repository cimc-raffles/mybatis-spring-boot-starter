package cimc.raffles.mybatis.interceptor;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.util.ReflectionUtils;

import cimc.raffles.mybatis.annotation.CreatedDate;
import cimc.raffles.mybatis.annotation.LastModifiedDate;

@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}) })
public class AuditingInterceptor implements Interceptor
{
	@Override
	public Object intercept(Invocation invocation) throws Throwable 
	{
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        if( null == parameter)
        	return invocation.proceed() ;

        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        
        Class<? extends Object> parameterClass = parameter.getClass() ;
        
        Field[] fields = parameterClass.getDeclaredFields();
        
        Field lastModifiedDateField = Arrays.stream(fields).filter( x-> x.isAnnotationPresent( LastModifiedDate.class)).findFirst().orElse( null) ;
        Field createdDateField = Arrays.stream(fields).filter( x-> x.isAnnotationPresent( CreatedDate.class)).findFirst().orElse( null) ;
        
        if( SqlCommandType.INSERT.equals( sqlCommandType))
        {
        	this.setFieldValue( lastModifiedDateField, parameter);
        	this.setFieldValue( createdDateField, parameter);
        }
        
        if( SqlCommandType.UPDATE.equals( sqlCommandType))
        {
            if( ! ParamMap.class.equals( parameterClass))
            	this.setFieldValue( lastModifiedDateField, parameter) ;
            
            //mybatis plus
            else
            {
            	ParamMap<?> map = (ParamMap<?>)parameter ;
            	Entry<String, ?> entity = map.entrySet().iterator().next() ;
            	Object o = entity.getValue() ;
            	fields = o.getClass().getDeclaredFields() ;
            	lastModifiedDateField = Arrays.stream(fields).filter( x-> x.isAnnotationPresent( LastModifiedDate.class)).findFirst().orElse( null) ;
            	this.setFieldValue( lastModifiedDateField, o) ;
            }
        }
        
		return invocation.proceed() ;
	}

	@Override
	public Object plugin(Object target) 
	{
		return target instanceof Executor ? Plugin.wrap(target, this) : target ;
	}

	@Override
	public void setProperties(Properties properties)
	{
		// TODO Auto-generated method stub
	}
	
	
	private void setFieldValue( Field field , Object target )
	{
		if( null==field)
			return ;
		
		Class<?> clazz = field.getType() ;
		
		field.setAccessible(true);
		
		if(  clazz.equals( LocalDateTime.class))
			ReflectionUtils.setField( field, target, LocalDateTime.now());
		else if ( clazz.equals( LocalDate.class))
			ReflectionUtils.setField( field, target, LocalDate.now());
		else if ( clazz.equals( Timestamp.class))
			ReflectionUtils.setField( field, target, Timestamp.from( Instant.now()));
		else if( clazz.equals( java.util.Date.class))
			ReflectionUtils.setField( field, target, Timestamp.from( Instant.now()));
		else if( clazz.equals( java.sql.Date.class))
			ReflectionUtils.setField( field, target, java.sql.Date.from( Instant.now()));
		else
			;
		
		field.setAccessible(false);
	}

}
