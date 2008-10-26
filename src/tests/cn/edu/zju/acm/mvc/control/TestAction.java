package cn.edu.zju.acm.mvc.control;

import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;

@Result(name = "success", value = "test", type = ResultType.Redirect)
public class TestAction extends TestActionBase {
}
