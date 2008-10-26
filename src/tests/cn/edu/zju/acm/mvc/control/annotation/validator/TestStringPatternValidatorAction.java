
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestAction;

public class TestStringPatternValidatorAction extends TestAction {

    String stringProp;

    String[] stringArrayProp;

    public String getStringProp() {
        return this.stringProp;
    }

    public String[] getStringArrayProp() {
        return this.stringArrayProp;
    }

    @StringPatternValidator(pattern = "a*")
    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }

    @StringPatternValidator(pattern = "a*")
    public void setStringArrayProp(String[] stringArrayProp) {
        this.stringArrayProp = stringArrayProp;
    }
}
