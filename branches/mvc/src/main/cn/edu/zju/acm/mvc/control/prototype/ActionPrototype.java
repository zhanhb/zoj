
package cn.edu.zju.acm.mvc.control.prototype;

import java.util.Date;
import java.util.List;
import java.util.Set;

import cn.edu.zju.acm.mvc.control.Action;

public class ActionPrototype extends Action {

    private int intProp;

    private long longProp;

    private String stringProp;

    private double doubleProp;

    public double getDoubleProp() {
        return this.doubleProp;
    }

    public void setDoubleProp(double doubleProp) {
        this.doubleProp = doubleProp;
    }

    private int[] intArrayProp;

    private float[] floatArrayProp;

    private long[] longArrayProp;

    public long[] getLongArrayProp() {
        return this.longArrayProp;
    }

    public void setLongArrayProp(long[] longArrayProp) {
        this.longArrayProp = longArrayProp;
    }

    private String[] stringArrayProp;

    public String[] getStringArrayProp() {
        return this.stringArrayProp;
    }

    public void setStringArrayProp(String[] stringArrayProp) {
        this.stringArrayProp = stringArrayProp;
    }

    public float[] getFloatArrayProp() {
        return this.floatArrayProp;
    }

    public void setFloatArrayProp(float[] floatArrayProp) {
        this.floatArrayProp = floatArrayProp;
    }

    public double[] getDoubleArrayProp() {
        return this.doubleArrayProp;
    }

    public void setDoubleArrayProp(double[] doubleArrayProp) {
        this.doubleArrayProp = doubleArrayProp;
    }

    private double[] doubleArrayProp;

    private Integer integerProp;

    public Integer getIntegerProp() {
        return this.integerProp;
    }

    public void setIntegerProp(Integer integerProp) {
        this.integerProp = integerProp;
    }

    private List<Integer> intListProp;

    private Set<Integer> intSetProp;

    private Date dateProp;

    public Date getDateProp() {
        return this.dateProp;
    }

    public void setDateProp(Date dateProp) {
        this.dateProp = dateProp;
    }

    public int[] getIntArrayProp() {
        return this.intArrayProp;
    }

    public void setIntArrayProp(int[] intArrayProp) {
        this.intArrayProp = intArrayProp;
    }

    public List<Integer> getIntListProp() {
        return this.intListProp;
    }

    public void setIntListProp(List<Integer> intListProp) {
        this.intListProp = intListProp;
    }

    public Set<Integer> getIntSetProp() {
        return this.intSetProp;
    }

    public void setIntSetProp(Set<Integer> intSetProp) {
        this.intSetProp = intSetProp;
    }

    public int getIntProp() {
        return this.intProp;
    }

    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    public long getLongProp() {
        return this.longProp;
    }

    public void setLongProp(long longProp) {
        this.longProp = longProp;
    }

    public String getStringProp() {
        return this.stringProp;
    }

    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }

    @Override
    public String execute() throws Exception {
        return null;
    }

}
