
package cn.edu.zju.acm.mvc.control;

public class FieldError {

    private String fieldName;

    private String messageKey;

    private String[] arguments;

    public FieldError(String fieldName, String messageKey, String[] arguments) {
        this.fieldName = fieldName;
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getMessageKey() {
        return this.messageKey;
    }

    
    public String[] getArguments() {
        return this.arguments;
    }
}
