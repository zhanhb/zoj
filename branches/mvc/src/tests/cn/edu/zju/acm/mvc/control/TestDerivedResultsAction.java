
package cn.edu.zju.acm.mvc.control;

import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;

@Result(name = "jsp", type = ResultType.Jsp, value = "derived.jsp")
public class TestDerivedResultsAction extends TestResultsAction {
}
