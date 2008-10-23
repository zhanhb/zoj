
package cn.edu.zju.acm.mvc.control;

import cn.edu.zju.acm.mvc.control.annotation.OneException;
import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;

@OneException(exception = IllegalArgumentException.class, result = "test")
@Result(name = "test", type = ResultType.Jsp, value = "test.jsp")
public class TestDerivedExceptionsAction extends TestExceptionsAction {
}
