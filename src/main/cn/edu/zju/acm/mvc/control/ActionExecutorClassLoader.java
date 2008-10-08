
package cn.edu.zju.acm.mvc.control;

public class ActionExecutorClassLoader extends ClassLoader {

    public ActionExecutorClassLoader() {
        super(ActionExecutorClassLoader.class.getClassLoader());
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ActionExecutor> defineClass(String name, byte[] b) {
        return (Class<? extends ActionExecutor>) super.defineClass(name, b, 0, b.length);
    }
}
