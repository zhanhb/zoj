
package cn.edu.zju.acm.mvc.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ActionProxy {
    
    String execute(HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
