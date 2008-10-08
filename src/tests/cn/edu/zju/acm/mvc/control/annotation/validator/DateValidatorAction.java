
package cn.edu.zju.acm.mvc.control.annotation.validator;

import java.util.Date;

import cn.edu.zju.acm.mvc.control.action.ActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.DateValidator;

public class DateValidatorAction extends ActionBase {

    Date dateProp;

    Date[] dateArrayProp;

    Date minProp;

    Date maxProp;

    @DateValidator(format = "yyyy-MM-dd")
    public void setDateProp(Date dateProp) {
        this.dateProp = dateProp;
    }

    @DateValidator(format = "yyyy-MM-dd")
    public void setDateArrayProp(Date[] dateArrayProp) {
        this.dateArrayProp = dateArrayProp;
    }

    @DateValidator(min = "2000-01-01", format = "yyyy-MM-dd")
    public void setMinProp(Date minProp) {
        this.minProp = minProp;
    }

    @DateValidator(max = "2000-01-01", format = "yyyy-MM-dd")
    public void setMaxProp(Date maxProp) {
        this.maxProp = maxProp;
    }
}
