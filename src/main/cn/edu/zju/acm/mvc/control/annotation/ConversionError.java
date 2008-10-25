
package cn.edu.zju.acm.mvc.control.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConversionError {

    String message();
}
