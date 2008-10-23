
package cn.edu.zju.acm.mvc.control;

import java.io.InputStream;

import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;
import cn.edu.zju.acm.mvc.control.annotation.Results;

@Results( {@Result(name = "jsp", type = ResultType.Jsp, value = "test.jsp"),
           @Result(name = "raw", type = ResultType.Raw, value = "out"),
           @Result(name = "redirect", type = ResultType.Redirect, value = "/test")})
public class TestResultsAction extends TestActionBase {

    private InputStream out;

    public InputStream getOut() {
        return this.out;
    }
}
