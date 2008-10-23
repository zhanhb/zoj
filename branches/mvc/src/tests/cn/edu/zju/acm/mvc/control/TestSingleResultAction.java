
package cn.edu.zju.acm.mvc.control;

import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;

@Result(name = "jsp", type = ResultType.Jsp, value = "test.jsp")
public class TestSingleResultAction extends TestActionBase {
}
