
package cn.edu.zju.acm.mvc.control.annotation.validator;

import cn.edu.zju.acm.mvc.control.TestAction;
import cn.edu.zju.acm.mvc.control.annotation.validator.DoubleRangeValidator;

public class TestDoubleRangeValidatorAction extends TestAction {

    double noneOnDoubleProp;

    double minOnDoubleProp;

    double maxOnDoubleProp;

    double minAndMaxOnDoubleProp;

    double[] noneOnDoubleArrayProp;

    double[] minOnDoubleArrayProp;

    double[] maxOnDoubleArrayProp;

    double[] minAndMaxOnDoubleArrayProp;

    public double getNoneOnDoubleProp() {
        return this.noneOnDoubleProp;
    }

    public double getMinOnDoubleProp() {
        return this.minOnDoubleProp;
    }

    public double getMaxOnDoubleProp() {
        return this.maxOnDoubleProp;
    }

    public double getMinAndMaxOnDoubleProp() {
        return this.minAndMaxOnDoubleProp;
    }

    public double[] getNoneOnDoubleArrayProp() {
        return this.noneOnDoubleArrayProp;
    }

    public double[] getMinOnDoubleArrayProp() {
        return this.minOnDoubleArrayProp;
    }

    public double[] getMaxOnDoubleArrayProp() {
        return this.maxOnDoubleArrayProp;
    }

    public double[] getMinAndMaxOnDoubleArrayProp() {
        return this.minAndMaxOnDoubleArrayProp;
    }

    @DoubleRangeValidator
    public void setNoneOnDoubleProp(double noneOnDoubleProp) {
        this.noneOnDoubleProp = noneOnDoubleProp;
    }

    @DoubleRangeValidator(min = 1)
    public void setMinOnDoubleProp(double minnProp) {
        this.minOnDoubleProp = minnProp;
    }

    @DoubleRangeValidator(max = 2)
    public void setMaxOnDoubleProp(double maxProp) {
        this.maxOnDoubleProp = maxProp;
    }

    @DoubleRangeValidator(min = 1, max = 2)
    public void setMinAndMaxOnDoubleProp(double minAndMaxProp) {
        this.minAndMaxOnDoubleProp = minAndMaxProp;
    }

    @DoubleRangeValidator
    public void setNoneOnDoubleArrayProp(double[] noneOnDoubleArrayProp) {
        this.noneOnDoubleArrayProp = noneOnDoubleArrayProp;
    }

    @DoubleRangeValidator(min = 1)
    public void setMinOnDoubleArrayProp(double[] minOnDoubleArrayProp) {
        this.minOnDoubleArrayProp = minOnDoubleArrayProp;
    }

    @DoubleRangeValidator(max = 2)
    public void setMaxOnDoubleArrayProp(double[] maxOnDoubleArrayProp) {
        this.maxOnDoubleArrayProp = maxOnDoubleArrayProp;
    }

    @DoubleRangeValidator(min = 1, max = 2)
    public void setMinAndMaxOnDoubleArrayProp(double[] minAndMaxOnDoubleArrayProp) {
        this.minAndMaxOnDoubleArrayProp = minAndMaxOnDoubleArrayProp;
    }
}
