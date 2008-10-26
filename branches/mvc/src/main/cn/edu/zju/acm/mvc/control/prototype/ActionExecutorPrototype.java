
package cn.edu.zju.acm.mvc.control.prototype;

import java.util.regex.Pattern;

public class ActionExecutorPrototype {

    private static Pattern p = Pattern.compile("");

    public long test(long i) throws Exception {
        return i;
    }

    public void execute() {
        p.matcher("").matches();
    }
}
