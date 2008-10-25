
package cn.edu.zju.acm.mvc.control.annotation.validator;

import java.util.Date;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.FloatRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidator;

public class ValidatorInvalidAction extends TestActionBase {

    String invalidTypeExceptStringProp;

    int invalidTypeStringProp;

    int intMaxLessThanMinProp;

    double floatMaxLessThanMinProp;

    String stringMaxLessThanMinProp;

    String stringNegativeMinProp;

    String stringInvalidPatternProp;

    Date dateMaxLessThanMinProp;

    Date dateInvalidMinProp;

    Date dateInvalidMaxProp;

    Date dateInvalidFormatProp;

    @IntRangeValidator
    @FloatRangeValidator
    public void setInvalidTypeExceptStringProp(String invalidTypeExceptStringProp) {
        this.invalidTypeExceptStringProp = invalidTypeExceptStringProp;
    }

    @StringLengthValidator
    public void setInvalidTypeStringProp(int invalidTypeStringProp) {
        this.invalidTypeStringProp = invalidTypeStringProp;
    }

    @IntRangeValidator(min = 2, max = 1)
    public void setIntMaxLessThanMinProp(int intMaxLessThanMinProp) {
        this.intMaxLessThanMinProp = intMaxLessThanMinProp;
    }

    @FloatRangeValidator(min = 2.0, max = 1.0)
    public void setFloatMaxLessThanMinProp(double floatMaxLessThanMinProp) {
        this.floatMaxLessThanMinProp = floatMaxLessThanMinProp;
    }

    @StringLengthValidator(minLength = 2, maxLength = 1)
    public void setStringMaxLessThanMinProp(String stringMaxLessThanMinProp) {
        this.stringMaxLessThanMinProp = stringMaxLessThanMinProp;
    }

    @StringLengthValidator(minLength = -1)
    public void setStringNegativeMinProp(String stringNegativeMinProp) {
        this.stringNegativeMinProp = stringNegativeMinProp;
    }

    @StringPatternValidator(pattern = "*")
    public void setStringInvalidPatternProp(String stringInvalidPatternProp) {
        this.stringInvalidPatternProp = stringInvalidPatternProp;
    }
}
