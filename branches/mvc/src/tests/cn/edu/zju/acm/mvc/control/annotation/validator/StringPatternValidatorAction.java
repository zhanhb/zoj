
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestActionBase;

public class StringPatternValidatorAction extends TestActionBase {

    String patternProp;

    @StringPatternValidator(pattern = "pattern")
    public void setPatternProp(String patternProp) {
        this.patternProp = patternProp;
    }
}
