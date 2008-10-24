
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

        final Map<String, ActionDescriptor> actionDescriptorMap = new HashMap<String, ActionDescriptor>();
        ActionUtil.loadActionClasses(actionDescriptorMap, actionPackageName, directory);
        for (Map.Entry<String, ActionDescriptor> entry : actionDescriptorMap.entrySet()) {
            final String key = entry.getKey();
            ActionDescriptor actionDescriptor = entry.getValue();
            actionDescriptor.buildAnnotationLists(new ResultFilter() {

                public boolean filter(String result) {
                    if (!result.startsWith("/")) {
                        result = key.substring(0, key.lastIndexOf('/') + 1) + result;
                    }
                    if (result.endsWith(".zar")) {
                        result = result.substring(0, result.length() - 1);
                    }
                    if (result.endsWith(".za")) {
                        if (!actionDescriptorMap.containsKey(result)) {
                            logger.error(result + " not found");
                            return false;
                        }
                        return true;
                    } else if (result.endsWith(".jsp")) {
                        try {
                            if (getServletContext().getResource(result) == null) {
                                logger.error(result + " not found");
                                return false;
                            }
                            return true;
                        } catch (MalformedURLException e) {
                            // Should not be here
                            logger.error(e);
                        }
                    }
                    logger.error("Invalid result " + result);
                    return false;
                }
            });
            this.actionFactoryMap.put(key, new ActionProxyFactory(actionDescriptor, debugMode != null));
        }
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        for (boolean forward = false;; forward = true) {
            ActionProxyFactory actionFactory = this.actionFactoryMap.get(path);
            if (actionFactory == null) {
                resp.sendError(404);
            } else {
                try {
                    ActionProxy action = actionFactory.newInstance();
                    String result = action.execute(req, resp, forward);
                    if (result.endsWith(".jsp")) {
                        req.getRequestDispatcher(result).forward(req, resp);
                    } else if (result.endsWith(".za")) {
                        path = result;
                        continue;
                    } else if (result.endsWith(".zar")) {
                        resp.sendRedirect(result.substring(0, result.length() - 1));
                    } else {
                        // Should not reach here
                        this.logger.error("Invalid result " + result);
                        resp.sendError(500);
                    }
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
            break;
        }
    }

}
