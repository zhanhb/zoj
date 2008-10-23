
package cn.edu.zju.acm.mvc.control.annotation.validator;

import java.util.Date;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.DateValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.FloatValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringValidator;

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

    @IntValidator
    @FloatValidator
    @DateValidator(format = "yyyy-MM-dd")
    public void setInvalidTypeExceptStringProp(String invalidTypeExceptStringProp) {
        this.invalidTypeExceptStringProp = invalidTypeExceptStringProp;
    }

    @StringValidator
    public void setInvalidTypeStringProp(int invalidTypeStringProp) {
        this.invalidTypeStringProp = invalidTypeStringProp;
    }

    @IntValidator(min = 2, max = 1)
    public void setIntMaxLessThanMinProp(int intMaxLessThanMinProp) {
        this.intMaxLessThanMinProp = intMaxLessThanMinProp;
    }

    @FloatValidator(min = 2.0, max = 1.0)
    public void setFloatMaxLessThanMinProp(double floatMaxLessThanMinProp) {
        this.floatMaxLessThanMinProp = floatMaxLessThanMinProp;
    }

    @StringValidator(minLength = 2, maxLength = 1)
    public void setStringMaxLessThanMinProp(String stringMaxLessThanMinProp) {
        this.stringMaxLessThanMinProp = stringMaxLessThanMinProp;
    }

    @StringValidator(minLength = -1)
    public void setStringNegativeMinProp(String stringNegativeMinProp) {
        this.stringNegativeMinProp = stringNegativeMinProp;
    }

    @StringValidator(pattern = "*")
    public void setStringInvalidPatternProp(String stringInvalidPatternProp) {
        this.stringInvalidPatternProp = stringInvalidPatternProp;
    }

    @DateValidator(min = "2000-01-01", max = "1999-12-31", format = "yyyy-MM-dd")
    public void setDateMaxLessThanMinProp(Date dateMaxLessThanMinProp) {
        this.dateMaxLessThanMinProp = dateMaxLessThanMinProp;
    }

    @DateValidator(min = "2000-01-", format = "yyyy-MM-dd")
    public void setDateInvalidMinProp(Date dateInvalidMinProp) {
        this.dateInvalidMinProp = dateInvalidMinProp;
    }

    @DateValidator(max = "2000-01-", format = "yyyy-MM-dd")
    public void setDateInvalidMaxProp(Date dateInvalidMaxProp) {
        this.dateInvalidMaxProp = dateInvalidMaxProp;
    }

    @DateValidator(format = "YYYY")
    public void setDateInvalidFormatProp(Date dateInvalidFormatProp) {
        this.dateInvalidFormatProp = dateInvalidFormatProp;
    }
}
