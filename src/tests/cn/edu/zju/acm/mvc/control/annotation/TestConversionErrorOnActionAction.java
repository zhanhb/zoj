
package cn.edu.zju.acm.mvc.control.annotation;

import cn.edu.zju.acm.mvc.control.TestActionBase;

@Result(name = "success", value = "", type = ResultType.Redirect)
public class TestConversionErrorOnActionAction extends TestActionBase {

    private int intProp;

    public int getIntProp() {
        return this.intProp;
    }

    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }
}
