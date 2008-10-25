
package cn.edu.zju.acm.mvc.control.annotation;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;
import cn.edu.zju.acm.mvc.control.annotation.Results;

@Results( {@Result(name = "test", type = ResultType.Jsp, value = "test.jsp"),
           @Result(name = "test", type = ResultType.Redirect, value = "/test")})
public class TestDuplicateResultsAction extends TestActionBase {
}
