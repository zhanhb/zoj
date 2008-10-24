
package cn.edu.zju.acm.mvc.control;


public class ActionProxyClassLoader extends ClassLoader {

    public ActionProxyClassLoader() {
        super(ActionProxyClassLoader.class.getClassLoader());
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ActionProxy> defineClass(String name, byte[] b) {
        return (Class<? extends ActionProxy>) super.defineClass(name, b, 0, b.length);
    }
}
