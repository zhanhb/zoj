
package cn.edu.zju.acm.mvc.control.annotation;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;

@Result(name = "raw", type = ResultType.Raw, value = "test")
public class TestRawResultInvalidPropertyTypeAction extends TestActionBase {
    private int test;

    public int getTest() {
        return this.test;
    }

    public void setTest(int test) {
        this.test = test;
    }
}
