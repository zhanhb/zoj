
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidator;

public class StringLengthValidatorAction extends TestActionBase {

    String stringProp;

    String[] stringArrayProp;

    String minLengthProp;

    String maxLengthProp;

    String patternProp;

    @StringLengthValidator
    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }

    @StringLengthValidator
    public void setStringArrayProp(String[] stringArrayProp) {
        this.stringArrayProp = stringArrayProp;
    }

    @StringLengthValidator(minLength = 1)
    public void setMinLengthProp(String minLengthProp) {
        this.minLengthProp = minLengthProp;
    }

    @StringLengthValidator(maxLength = 2)
    public void setMaxLengthProp(String maxLengthProp) {
        this.maxLengthProp = maxLengthProp;
    }
}
