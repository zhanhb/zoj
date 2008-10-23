
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntValidator;

public class IntValidatorAction extends TestActionBase {

    int intProp;

    long longProp;

    int[] intArrayProp;

    long[] longArrayProp;

    int minProp;

    int maxProp;

    @IntValidator
    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    @IntValidator
    public void setLongProp(long longProp) {
        this.longProp = longProp;
    }

    @IntValidator
    public void setIntArrayProp(int[] intArrayProp) {
        this.intArrayProp = intArrayProp;
    }

    @IntValidator
    public void setLongArrayProp(long[] longArrayProp) {
        this.longArrayProp = longArrayProp;
    }

    @IntValidator(min = 1)
    public void setMinProp(int minnProp) {
        this.minProp = minnProp;
    }

    @IntValidator(max = 2)
    public void setMaxProp(int maxProp) {
        this.maxProp = maxProp;
    }
}
