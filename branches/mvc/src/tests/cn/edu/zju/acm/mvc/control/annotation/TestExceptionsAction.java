
package cn.edu.zju.acm.mvc.control.annotation;

import cn.edu.zju.acm.mvc.control.annotation.Exceptions;
import cn.edu.zju.acm.mvc.control.annotation.OneException;

@Exceptions( {@OneException(exception = IllegalArgumentException.class, result = "jsp"),
              @OneException(exception = NullPointerException.class, result = "jsp")})
public class TestExceptionsAction extends TestExceptionActionBase {
}
