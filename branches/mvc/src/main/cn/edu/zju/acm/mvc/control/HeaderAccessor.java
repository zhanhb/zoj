
package cn.edu.zju.acm.mvc.control;

import java.util.Enumeration;

public interface HeaderAccessor {

    String getHeader(String name);

    Enumeration<String> getHeaders(String name);

    void addHeader(String name, String value);

    void setHeader(String name, String value);
}
