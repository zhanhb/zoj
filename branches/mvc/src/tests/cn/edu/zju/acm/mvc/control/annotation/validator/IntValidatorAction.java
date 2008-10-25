
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntRangeValidator;

public class IntValidatorAction extends TestActionBase {

    int intProp;

    long longProp;

    int[] intArrayProp;

    long[] longArrayProp;

    int minProp;

    int maxProp;

    @IntRangeValidator
    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    @IntRangeValidator
    public void setLongProp(long longProp) {
        this.longProp = longProp;
    }

    @IntRangeValidator
    public void setIntArrayProp(int[] intArrayProp) {
        this.intArrayProp = intArrayProp;
    }

    @IntRangeValidator
    public void setLongArrayProp(long[] longArrayProp) {
        this.longArrayProp = longArrayProp;
    }

    @IntRangeValidator(min = 1)
    public void setMinProp(int minnProp) {
        this.minProp = minnProp;
    }

    @IntRangeValidator(max = 2)
    public void setMaxProp(int maxProp) {
        this.maxProp = maxProp;
    }
}
