
package cn.edu.zju.acm.mvc.control;

import java.util.ArrayList;
import java.util.List;

import cn.edu.zju.acm.mvc.control.annotation.ConversionError;

@ConversionError(message="error.conversion")
public abstract class Action {

    public static final String SUCCESS = "success";

    public static final String FAILURE = "failure";

    public static final String INPUT = "input";

    public static final String EXCEPTION = "exception";

    private List<FieldError> fieldErrors = new ArrayList<FieldError>();

    public List<FieldError> getFieldErrors() {
        return this.fieldErrors;
    }

    protected void addFieldError(String name, String messageKey, String[] arguments) {
        this.fieldErrors.add(new FieldError(name, messageKey, arguments));
    }

    public abstract String execute() throws Exception;
}
