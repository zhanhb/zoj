
package cn.edu.zju.acm.mvc.control.annotation;

import cn.edu.zju.acm.mvc.control.TestActionBase;
import cn.edu.zju.acm.mvc.control.annotation.Cookie;

public class TestCookiePropertyAction extends TestActionBase {

    private int intProp;

    private String stringProp;

    private Object objProp;

    @Cookie
    public int getIntProp() {
        return this.intProp;
    }

    @Cookie
    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    @Cookie
    public String getStringProp() {
        return this.stringProp;
    }

    @Cookie
    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }

    @Cookie
    public Object getObjProp() {
        return this.objProp;
    }

    @Cookie
    public void setObjProp(Object objProp) {
        this.objProp = objProp;
    }

}
