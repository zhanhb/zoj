
package cn.edu.zju.acm.mvc.control;

import javax.servlet.http.Cookie;

public interface CookieAccessor {

    Cookie[] getCookies();

    void addCookie(Cookie cookie);
}
