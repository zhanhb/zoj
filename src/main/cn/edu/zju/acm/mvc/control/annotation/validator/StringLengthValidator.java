
package cn.edu.zju.acm.mvc.control.annotation.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StringLengthValidator {

    int minLength() default 0;

    int maxLength() default Integer.MAX_VALUE;
    
    String message() default "error.validation.stringlength";
}
