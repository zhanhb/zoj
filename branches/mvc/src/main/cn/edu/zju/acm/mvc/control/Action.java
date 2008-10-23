
package cn.edu.zju.acm.mvc.control;

import java.util.ArrayList;
import java.util.List;

public abstract class Action {

    public static final String SUCCESS = "success";

    public static final String FAILURE = "failure";

    public static final String INPUT = "input";

    public static final String EXCEPTION = "exception";

    private List<String> errorMessages = new ArrayList<String>();

    public List<String> getErrorMessages() {
        return this.errorMessages;
    }

    protected void addErrorMessage(String errorMessage) {
        this.errorMessages.add(errorMessage);
    }
    
    public abstract String execute() throws Exception;
}
