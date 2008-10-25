
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class ActionServlet extends HttpServlet {

    private Map<String, ActionProxyFactory> actionFactoryMap = new HashMap<String, ActionProxyFactory>();

    private Logger logger = Logger.getLogger(ActionServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.process(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String actionPackageName = config.getInitParameter("package");
        if (actionPackageName == null) {
            this.logger.error("No package defined");
            return;
        }

        String debugMode = config.getInitParameter("debug");

        if (!actionPackageName.startsWith("/")) {
            actionPackageName = "/" + actionPackageName;
        }
        actionPackageName = actionPackageName.replace('.', '/');
        URL url = ActionServlet.class.getResource(actionPackageName);
        if (url == null) {
            this.logger.error("Can not find resource for " + actionPackageName);
            return;
        }

        File directory = new File(url.getFile());
        if (!directory.exists()) {
            this.logger.error("Directory " + directory.getAbsolutePath() + "does not exist");
            return;
        }
        
        String defaultDateFormat = config.getInitParameter("default-date-format");

        final Map<String, ActionDescriptor> actionDescriptorMap = new HashMap<String, ActionDescriptor>();
        ActionUtil.loadActionClasses(actionDescriptorMap, actionPackageName, directory);
        for (Map.Entry<String, ActionDescriptor> entry : actionDescriptorMap.entrySet()) {
            final String key = entry.getKey();
            ActionDescriptor actionDescriptor = entry.getValue();
            this.actionFactoryMap.put(key, new ActionProxyFactory(actionDescriptor, defaultDateFormat, debugMode != null));
        }
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        ActionProxyFactory actionFactory = this.actionFactoryMap.get(path);
        if (actionFactory == null) {
            resp.sendError(404);
        } else {
            try {
                ActionProxy action = actionFactory.newInstance();
                action.execute(req, resp);
            } catch (ActionInstantiationException e) {
                resp.sendError(500);
            } catch (InvalidResultException e) {
                this.logger.error("Invalid result", e);
                resp.sendError(500);
            } catch (Throwable e) {
                this.logger.error("Unhandled exception", e);
                resp.sendError(500);
            }
        }
    }

}
