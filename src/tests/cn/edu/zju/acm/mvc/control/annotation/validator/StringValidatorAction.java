
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.action.ActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringValidator;

public class StringValidatorAction extends ActionBase {

    String stringProp;

    String[] stringArrayProp;

    String minLengthProp;

    String maxLengthProp;

    String patternProp;

    @StringValidator
    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }

    @StringValidator
    public void setStringArrayProp(String[] stringArrayProp) {
        this.stringArrayProp = stringArrayProp;
    }

    @StringValidator(minLength = 1)
    public void setMinLengthProp(String minLengthProp) {
        this.minLengthProp = minLengthProp;
    }

    @StringValidator(maxLength = 2)
    public void setMaxLengthProp(String maxLengthProp) {
        this.maxLengthProp = maxLengthProp;
    }

    @StringValidator(pattern = "pattern")
    public void setPatternProp(String patternProp) {
        this.patternProp = patternProp;
    }
}
