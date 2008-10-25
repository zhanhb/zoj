
package cn.edu.zju.acm.mvc.control.annotation;

@ConversionError(message = "error")
public class TestConversionErrorOnActionOverriddenAction extends TestConversionErrorOnActionAction {

    private int intProp;

    public int getIntProp() {
        return this.intProp;
    }

    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }
}
