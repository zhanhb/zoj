
package cn.edu.zju.acm.mvc.control;

public class MockActionDescriptor extends ActionDescriptor {
    private String conversionErrorMessageKey = null;
    public MockActionDescriptor(Class<? extends Action> actionClass) {
        super(actionClass);
    }

    public String getConversionErrorMessageKey() {
        return this.conversionErrorMessageKey;
    }

    public void setConversionErrorMessageKey(String conversionErrorMessageKey) {
        this.conversionErrorMessageKey = conversionErrorMessageKey;
    }
}
