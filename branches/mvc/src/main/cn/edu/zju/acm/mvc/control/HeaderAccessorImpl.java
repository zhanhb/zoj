
package cn.edu.zju.acm.mvc.control;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HeaderAccessorImpl implements HeaderAccessor {

    private HttpServletRequest req;

    private HttpServletResponse resp;

    public HeaderAccessorImpl(HttpServletRequest req, HttpServletResponse resp) {
        this.req = req;
        this.resp = resp;
    }

    public void addHeader(String name, String value) {
        this.resp.addHeader(name, value);
    }

    public String getHeader(String name) {
        return this.req.getHeader(name);
    }

    @SuppressWarnings("unchecked")
    public Enumeration<String> getHeaders(String name) {
        return this.req.getHeaders(name);
    }

    public void setHeader(String name, String value) {
        this.resp.setHeader(name, value);
    }

}
