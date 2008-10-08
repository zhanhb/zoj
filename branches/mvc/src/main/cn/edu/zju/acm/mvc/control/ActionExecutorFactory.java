
package cn.edu.zju.acm.mvc.control;

import org.apache.log4j.Logger;

public class ActionExecutorFactory {

    private Class<? extends ActionExecutor> clazz;

    private Logger logger = Logger.getLogger(ActionExecutorFactory.class);

    public ActionExecutorFactory(ActionDescriptor actionDescriptor, boolean debugMode) {
        this.clazz = new ActionExecutorBuilder().build(actionDescriptor, debugMode);
    }

    public ActionExecutor newInstance() throws ActionInstantiationException {
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
