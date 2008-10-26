
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestAction;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntRangeValidator;

public class TestIntRangeValidatorAction extends TestAction {

    int noneOnIntProp;

    int minOnIntProp;

    int maxOnIntProp;

    int minAndMaxOnIntProp;

    int[] noneOnIntArrayProp;

    int[] minOnIntArrayProp;

    int[] maxOnIntArrayProp;

    int[] minAndMaxOnIntArrayProp;

    public int getNoneOnIntProp() {
        return this.noneOnIntProp;
    }

    public int getMinOnIntProp() {
        return this.minOnIntProp;
    }

    public int getMaxOnIntProp() {
        return this.maxOnIntProp;
    }

    public int getMinAndMaxOnIntProp() {
        return this.minAndMaxOnIntProp;
    }

    public int[] getNoneOnIntArrayProp() {
        return this.noneOnIntArrayProp;
    }

    public int[] getMinOnIntArrayProp() {
        return this.minOnIntArrayProp;
    }

    public int[] getMaxOnIntArrayProp() {
        return this.maxOnIntArrayProp;
    }

    public int[] getMinAndMaxOnIntArrayProp() {
        return this.minAndMaxOnIntArrayProp;
    }

    @IntRangeValidator
    public void setNoneOnIntProp(int noneOnIntProp) {
        this.noneOnIntProp = noneOnIntProp;
    }

    @IntRangeValidator(min = 1)
    public void setMinOnIntProp(int minnProp) {
        this.minOnIntProp = minnProp;
    }

    @IntRangeValidator(max = 2)
    public void setMaxOnIntProp(int maxProp) {
        this.maxOnIntProp = maxProp;
    }

    @IntRangeValidator(min = 1, max = 2)
    public void setMinAndMaxOnIntProp(int minAndMaxProp) {
        this.minAndMaxOnIntProp = minAndMaxProp;
    }

    @IntRangeValidator
    public void setNoneOnIntArrayProp(int[] noneOnIntArrayProp) {
        this.noneOnIntArrayProp = noneOnIntArrayProp;
    }

    @IntRangeValidator(min = 1)
    public void setMinOnIntArrayProp(int[] minOnIntArrayProp) {
        this.minOnIntArrayProp = minOnIntArrayProp;
    }

    @IntRangeValidator(max = 2)
    public void setMaxOnIntArrayProp(int[] maxOnIntArrayProp) {
        this.maxOnIntArrayProp = maxOnIntArrayProp;
    }

    @IntRangeValidator(min = 1, max = 2)
    public void setMinAndMaxOnIntArrayProp(int[] minAndMaxOnIntArrayProp) {
        this.minAndMaxOnIntArrayProp = minAndMaxOnIntArrayProp;
    }
}
