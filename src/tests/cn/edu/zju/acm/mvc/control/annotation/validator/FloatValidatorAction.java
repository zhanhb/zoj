
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.FloatRangeValidator;

public class FloatValidatorAction extends TestActionBase {

    double doubleProp;

    double[] doubleArrayProp;

    double minProp;

    double maxProp;

    @FloatRangeValidator
    public void setDoubleProp(double doubleProp) {
        this.doubleProp = doubleProp;
    }

    @FloatRangeValidator
    public void setDoubleArrayProp(double[] doubleArrayProp) {
        this.doubleArrayProp = doubleArrayProp;
    }

    @FloatRangeValidator(min = 1.0)
    public void setMinProp(double minProp) {
        this.minProp = minProp;
    }

    @FloatRangeValidator(max = 2.0)
    public void setMaxProp(double maxProp) {
        this.maxProp = maxProp;
    }
}
