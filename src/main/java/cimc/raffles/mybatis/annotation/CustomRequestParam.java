package cimc.raffles.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.databind.DeserializationFeature;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CustomRequestParam
{
	DeserializationFeature[] enable() default {} ;
	
	DeserializationFeature[] disable() default {} ; 
}
