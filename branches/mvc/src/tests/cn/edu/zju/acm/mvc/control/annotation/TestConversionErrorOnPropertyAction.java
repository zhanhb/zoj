
package cn.edu.zju.acm.mvc.control.annotation;

import cn.edu.zju.acm.mvc.control.TestActionBase;

public class TestConversionErrorOnPropertyAction extends TestActionBase {

    private int intProp;

    private double doubleProp;

    private String stringProp;

    @ConversionError(message = "error")
    public int getIntProp() {
        return this.intProp;
    }

    @ConversionError(message = "error")
    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    public double getDoubleProp() {
        return this.doubleProp;
    }

    public void setDoubleProp(double doubleProp) {
        this.doubleProp = doubleProp;
    }

    @ConversionError(message = "error")
    public String getStringProp() {
        return this.stringProp;
    }

    @ConversionError(message = "error")
    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }
}
