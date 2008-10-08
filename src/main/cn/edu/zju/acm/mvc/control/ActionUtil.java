
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

public class ActionUtil {

    private static Logger logger = Logger.getLogger(ActionUtil.class);

    @SuppressWarnings("unchecked")
    public static void loadActionClasses(Map<String, ActionDescriptor> actionDescriptorMap, String packagePrefix,
                                         File baseDir) {
        for (File file : baseDir.listFiles()) {
            if (file.isDirectory()) {
                ActionUtil.loadActionClasses(actionDescriptorMap, packagePrefix + "." + file.getName(), file);
            } else if (file.getName().endsWith(".class")) {
                String className = packagePrefix + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<? extends Action> clazz = (Class<? extends Action>) Class.forName(className);
                    try {
                        clazz.getConstructor();
                    } catch (SecurityException e) {
                        ActionUtil.logger.error("Class " + className + " should have a public default constructor", e);
                    } catch (NoSuchMethodException e) {
                        ActionUtil.logger.error("Class " + className + " should have a public default constructor", e);
                    }
                    actionDescriptorMap.put("/" + className.replace(".", "/") + ".za", new ActionDescriptor(clazz));
                } catch (ClassNotFoundException e) {
                    ActionUtil.logger.error(e);
                } catch (ClassCastException e) {
                    ActionUtil.logger.debug("Ignore class " + className + ". Not a subclass of Action");
                }
            }
        }
    }

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
