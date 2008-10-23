
package cn.edu.zju.acm.mvc.control.annotation.validator;

import java.io.File;
import java.util.Date;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.validator.Required;

public class RequiredAction extends TestActionBase {

    int intProp;

    long longProp;

    int[] intArrayProp;

    long[] longArrayProp;

    double doubleProp;

    double[] doubleArrayProp;

    String stringProp;

    String[] stringArrayProp;

    Date dateProp;

    Date[] dateArrayProp;

    File fileProp;

    @Required
    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    @Required    
    public void setLongProp(long longProp) {
        this.longProp = longProp;
    }

    @Required
    public void setIntArrayProp(int[] intArrayProp) {
        this.intArrayProp = intArrayProp;
    }

    @Required
    public void setLongArrayProp(long[] longArrayProp) {
        this.longArrayProp = longArrayProp;
    }

    @Required
    public void setDoubleProp(double doubleProp) {
        this.doubleProp = doubleProp;
    }

    @Required
    public void setDoubleArrayProp(double[] doubleArrayProp) {
        this.doubleArrayProp = doubleArrayProp;
    }

    @Required    
    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }

    @Required
    public void setStringArrayProp(String[] stringArrayProp) {
        this.stringArrayProp = stringArrayProp;
    }

    @Required    
    public void setDateProp(Date dateProp) {
        this.dateProp = dateProp;
    }

    @Required
    public void setDateArrayProp(Date[] dateArrayProp) {
        this.dateArrayProp = dateArrayProp;
    }

    @Required
    public void setFileProp(File fileProp) {
        this.fileProp = fileProp;
    }
}
