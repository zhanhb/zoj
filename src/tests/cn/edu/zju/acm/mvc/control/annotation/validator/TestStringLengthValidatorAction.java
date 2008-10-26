
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestAction;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidator;

public class TestStringLengthValidatorAction extends TestAction {

    String noneOnStringProp;

    String minOnStringProp;

    String maxOnStringProp;

    String minAndMaxOnStringProp;

    String[] noneOnStringArrayProp;

    String[] minOnStringArrayProp;

    String[] maxOnStringArrayProp;

    String[] minAndMaxOnStringArrayProp;

    public String getNoneOnStringProp() {
        return this.noneOnStringProp;
    }

    public String getMinOnStringProp() {
        return this.minOnStringProp;
    }

    public String getMaxOnStringProp() {
        return this.maxOnStringProp;
    }

    public String getMinAndMaxOnStringProp() {
        return this.minAndMaxOnStringProp;
    }

    public String[] getNoneOnStringArrayProp() {
        return this.noneOnStringArrayProp;
    }

    public String[] getMinOnStringArrayProp() {
        return this.minOnStringArrayProp;
    }

    public String[] getMaxOnStringArrayProp() {
        return this.maxOnStringArrayProp;
    }

    public String[] getMinAndMaxOnStringArrayProp() {
        return this.minAndMaxOnStringArrayProp;
    }

    @StringLengthValidator
    public void setNoneOnStringProp(String noneOnStringProp) {
        this.noneOnStringProp = noneOnStringProp;
    }

    @StringLengthValidator(min = 1)
    public void setMinOnStringProp(String minnProp) {
        this.minOnStringProp = minnProp;
    }

    @StringLengthValidator(max = 2)
    public void setMaxOnStringProp(String maxProp) {
        this.maxOnStringProp = maxProp;
    }

    @StringLengthValidator(min = 1, max = 2)
    public void setMinAndMaxOnStringProp(String minAndMaxProp) {
        this.minAndMaxOnStringProp = minAndMaxProp;
    }

    @StringLengthValidator
    public void setNoneOnStringArrayProp(String[] noneOnStringArrayProp) {
        this.noneOnStringArrayProp = noneOnStringArrayProp;
    }

    @StringLengthValidator(min = 1)
    public void setMinOnStringArrayProp(String[] minOnStringArrayProp) {
        this.minOnStringArrayProp = minOnStringArrayProp;
    }

    @StringLengthValidator(max = 2)
    public void setMaxOnStringArrayProp(String[] maxOnStringArrayProp) {
        this.maxOnStringArrayProp = maxOnStringArrayProp;
    }

    @StringLengthValidator(min = 1, max = 2)
    public void setMinAndMaxOnStringArrayProp(String[] minAndMaxOnStringArrayProp) {
        this.minAndMaxOnStringArrayProp = minAndMaxOnStringArrayProp;
    }
}
