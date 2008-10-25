
package cn.edu.zju.acm.mvc.control;

import org.apache.log4j.Logger;


public class ActionProxyFactory {

    private Class<? extends ActionProxy> clazz;

    private Logger logger = Logger.getLogger(ActionProxyFactory.class);

    public ActionProxyFactory(ActionDescriptor actionDescriptor, String defaultDateFormat, boolean debugMode) {
        this.clazz = new ActionProxyBuilder().build(actionDescriptor, defaultDateFormat, debugMode);
    }

    public ActionProxy newInstance() throws ActionInstantiationException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            this.logger.error(e);
            throw new ActionInstantiationException();
        } catch (IllegalAccessException e) {
            this.logger.error(e);
            throw new ActionInstantiationException();
        }
    }
}
