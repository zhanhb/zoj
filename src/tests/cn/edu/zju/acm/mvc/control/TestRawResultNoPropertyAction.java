
package cn.edu.zju.acm.mvc.control;

import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;

@Result(name = "raw", type = ResultType.Raw, value = "test")
public class TestRawResultNoPropertyAction extends TestActionBase {
}
