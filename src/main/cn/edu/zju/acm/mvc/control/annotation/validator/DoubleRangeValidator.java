
package cn.edu.zju.acm.mvc.control.annotation.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DoubleRangeValidator {

    double min() default Double.MIN_VALUE;
    
    double max() default Double.MAX_VALUE;
    
    String message() default "error.validation.floatrange";
}
