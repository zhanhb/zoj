
package cn.edu.zju.acm.mvc.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

public abstract class Action {

    public static final String SUCCESS = "success";

    public static final String FAILURE = "failure";

    public static final String INPUT = "input";

    public static final String EXCEPTION = "exception";

    private List<String> errorMessages = new ArrayList<String>();
    
    protected Map<String, Cookie> cookieMap = null;
    
    protected List<Cookie> outputCookies = null;

    public List<String> getErrorMessages() {
        return this.errorMessages;
    }

    protected void addErrorMessage(String errorMessage) {
        this.errorMessages.add(errorMessage);
    }
}
