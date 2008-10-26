
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.DoubleRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidator;

public class ValidatorInvalidAction extends TestActionBase {

    String invalidTypeExceptStringProp;

    int invalidTypeStringProp;

    long intRangeOnLongProp;

    int intMaxLessThanMinProp;

    double doubleMaxLessThanMinProp;

    String stringMaxLessThanMinProp;

    String stringNegativeMinProp;

    String stringInvalidPatternProp;

    @IntRangeValidator
    @DoubleRangeValidator
    public void setInvalidTypeExceptStringProp(String invalidTypeExceptStringProp) {
        this.invalidTypeExceptStringProp = invalidTypeExceptStringProp;
    }

    @StringLengthValidator
    @StringPatternValidator(pattern = "")
    public void setInvalidTypeStringProp(int invalidTypeStringProp) {
        this.invalidTypeStringProp = invalidTypeStringProp;
    }

    @IntRangeValidator
    public void setIntRangeOnLongProp(long intRangeOnLongProp) {
        this.intRangeOnLongProp = intRangeOnLongProp;
    }

    @IntRangeValidator(min = 2, max = 1)
    public void setIntMaxLessThanMinProp(int intMaxLessThanMinProp) {
        this.intMaxLessThanMinProp = intMaxLessThanMinProp;
    }

    @DoubleRangeValidator(min = 2.0, max = 1.0)
    public void setDoubleMaxLessThanMinProp(double floatMaxLessThanMinProp) {
        this.doubleMaxLessThanMinProp = floatMaxLessThanMinProp;
    }

    @StringLengthValidator(min = 2, max = 1)
    public void setStringMaxLessThanMinProp(String stringMaxLessThanMinProp) {
        this.stringMaxLessThanMinProp = stringMaxLessThanMinProp;
    }

    @StringLengthValidator(min = -1)
    public void setStringNegativeMinProp(String stringNegativeMinProp) {
        this.stringNegativeMinProp = stringNegativeMinProp;
    }

    @StringPatternValidator(pattern = "*")
    public void setStringInvalidPatternProp(String stringInvalidPatternProp) {
        this.stringInvalidPatternProp = stringInvalidPatternProp;
    }
    
    @StringPatternValidator(pattern = "")
    public void setStringEmptyPatternProp(String stringInvalidPatternProp) {
        this.stringInvalidPatternProp = stringInvalidPatternProp;
    }
}
