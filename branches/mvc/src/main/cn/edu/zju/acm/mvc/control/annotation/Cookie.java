
package cn.edu.zju.acm.mvc.control.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cookie {

    int maxAge() default -1;

    String domain() default "";

    String path() default "";

    boolean secure() default false;

    int version() default 0;
}
