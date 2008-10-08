
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.action.ActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.FloatValidator;

public class FloatValidatorAction extends ActionBase {

    double doubleProp;

    double[] doubleArrayProp;

    double minProp;

    double maxProp;

    @FloatValidator
    public void setDoubleProp(double doubleProp) {
        this.doubleProp = doubleProp;
    }

    @FloatValidator
    public void setDoubleArrayProp(double[] doubleArrayProp) {
        this.doubleArrayProp = doubleArrayProp;
    }

    @FloatValidator(min = 1.0)
    public void setMinProp(double minProp) {
        this.minProp = minProp;
    }

    @FloatValidator(max = 2.0)
    public void setMaxProp(double maxProp) {
        this.maxProp = maxProp;
    }
}
