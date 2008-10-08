
package cn.edu.zju.acm.mvc.control;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieAccessorImpl implements CookieAccessor {

    private HttpServletRequest req;

    private HttpServletResponse resp;

    public CookieAccessorImpl(HttpServletRequest req, HttpServletResponse resp) {
        this.req = req;
        this.resp = resp;
    }

    public void addCookie(Cookie cookie) {
        this.resp.addCookie(cookie);
    }

    public Cookie[] getCookies() {
        return this.req.getCookies();
    }

}
