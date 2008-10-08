
package cn.edu.zju.acm.mvc.control;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

    private Map<String, Object> attributeMap = new HashMap<String, Object>();

    public Object getAttribute(String attributenName) {
        return this.attributeMap.get(attributenName);
    }

    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        return null;
    }

    public long getCreationTime() {
        return 0;
    }

    public String getId() {
        return null;
    }

    public long getLastAccessedTime() {
        return 0;
    }

    public int getMaxInactiveInterval() {
        return 0;
    }

    public ServletContext getServletContext() {
        return null;
    }

    public HttpSessionContext getSessionContext() {
        return null;
    }

    public Object getValue(String arg0) {
        return null;
    }

    public String[] getValueNames() {
        return null;
    }

    public void invalidate() {
    }

    public boolean isNew() {
        return false;
    }

    public void putValue(String arg0, Object arg1) {
    }

    public void removeAttribute(String arg0) {
    }

    public void removeValue(String arg0) {
    }

    public void setAttribute(String attributenName, Object attributenValue) {
        this.attributeMap.put(attributenName, attributenValue);
    }

    public void setMaxInactiveInterval(int arg0) {
    }
}
