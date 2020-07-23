package cimc.raffles.mybatis.annotation;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cimc.raffles.mybatis.enumeration.Creator;

/**
 * Declares a field as the one representing the principal that recently modified the entity containing the field.
 *
 * @author Ranie Jade Ramiso
 * @author Oliver Gierke
 * @since 1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD, METHOD, ANNOTATION_TYPE })
public @interface LastModifiedBy 
{
	Creator value() default Creator.ID ;
}
