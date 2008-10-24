
package cn.edu.zju.acm.mvc.control;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.edu.zju.acm.mvc.control.annotation.Exceptions;
import cn.edu.zju.acm.mvc.control.annotation.OneException;
import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;
import cn.edu.zju.acm.mvc.control.annotation.Results;

public class ActionDescriptor {

    private Logger logger = Logger.getLogger(PropertyDescriptor.class);

    private Class<? extends Action> actionClass;

    private Map<String, Result> resultMap = new LinkedHashMap<String, Result>();

    private Map<Result, Class<?>> resultClassMap = new HashMap<Result, Class<?>>();

    private Map<Class<? extends Throwable>, OneException> exceptionMap =
            new LinkedHashMap<Class<? extends Throwable>, OneException>();

    private Map<OneException, Class<?>> exceptionClassMap = new HashMap<OneException, Class<?>>();

    private Map<String, PropertyDescriptor> inputPropertyMap = new HashMap<String, PropertyDescriptor>();

    private Map<String, PropertyDescriptor> outputPropertyMap = new HashMap<String, PropertyDescriptor>();

    private ActionDescriptor(Class<? extends Action> actionClass) {
        int modifiers = actionClass.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            this.logger.debug("Skip abstract class " + actionClass.getName());
            return;
        }
        if (Modifier.isFinal(modifiers)) {
            this.logger.warn("Skip final class " + actionClass.getName());
            return;
        }
        if (!Modifier.isPublic(modifiers)) {
            this.logger.warn("Skip non-public class " + actionClass.getName());
            return;
        }
        try {
            actionClass.getConstructor();
        } catch (Exception e) {
            this.logger.error("No public default constructor defined for action class " + actionClass.getName(), e);
            return;
        }
        this.actionClass = actionClass;
        for (PropertyDescriptor propertyDescriptor : PropertyDescriptor.getInputProperties(actionClass)) {
            this.inputPropertyMap.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        for (PropertyDescriptor propertyDescriptor : PropertyDescriptor.getOutputProperties(actionClass)) {
            this.outputPropertyMap.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        this.initResultMap();
        this.initExecpetionMap();
        if (this.resultMap.size() == 0) {
            this.logger.error("No results defined for action class " + actionClass.getName());
        }
    }

    public static ActionDescriptor getActionDescriptor(Class<? extends Action> actionClass) {
        ActionDescriptor ret = new ActionDescriptor(actionClass);
        if (ret.resultMap.size() == 0) {
            return null;
        }
        return ret;
    }

    public Class<? extends Action> getActionClass() {
        return this.actionClass;
    }

    public Map<String, Result> getResultMap() {
        return this.resultMap;
    }

    public Map<Class<? extends Throwable>, OneException> getExceptionMap() {
        return this.exceptionMap;
    }

    public Map<String, PropertyDescriptor> getInputPropertyMap() {
        return this.inputPropertyMap;
    }

    public Map<String, PropertyDescriptor> getOutputPropertyMap() {
        return this.outputPropertyMap;
    }

    private void initResultMap() {
        for (Class<?> clazz = this.actionClass; !Action.class.equals(clazz); clazz = clazz.getSuperclass()) {
            for (Annotation annotation : clazz.getAnnotations()) {
                if (annotation instanceof Results) {
                    for (Result result : ((Results) annotation).value()) {
                        this.addResult(clazz, result);
                    }
                } else if (annotation instanceof Result) {
                    this.addResult(clazz, (Result) annotation);
                }
            }
        }
    }
    
    private void addResult(Class<?> clazz, Result result) {
        if (result.type() == ResultType.Raw) {
            PropertyDescriptor propertyDescriptor = this.outputPropertyMap.get(result.value());
            if (propertyDescriptor == null) {
                this.logger.error(String.format("Invalid result %s@%s. No output property \"%s\" found.",
                                                result.toString(), clazz.getName(), result.value()));
                return;
            } else if (!InputStream.class.isAssignableFrom(propertyDescriptor.getRawType())) {
                this.logger.error(String.format("Invalid result %s@%s. Property \"%s\" should be InputStream.",
                                                result.toString(), clazz.getName(), result.value()));
                return;
            }
        }
        
        String resultName = result.name();
        Result oldResult = this.resultMap.get(resultName);
        if (oldResult == null) {
            this.resultMap.put(resultName, result);
            this.resultClassMap.put(result, clazz);
        } else {
            Class<?> oldClazz = this.resultClassMap.get(oldResult);
            if (clazz.equals(oldClazz)) {
                this.logger.error(String.format("Duplicate result name \"%s\" in %s", resultName,
                                                clazz.getName()));
            } else {
                this.logger.debug(String.format("%s@%s is overridden by %s@%s", result.toString(),
                                                clazz.getName(), oldResult.toString(), oldClazz.getName()));
            }
        }
    }

    private void initExecpetionMap() {
        for (Class<?> clazz = this.actionClass; !Action.class.equals(clazz); clazz = clazz.getSuperclass()) {
            for (Annotation annotation : clazz.getAnnotations()) {
                if (annotation instanceof Exceptions) {
                    for (OneException exception : ((Exceptions) annotation).value()) {
                        this.addException(clazz, exception);
                    }
                } else if (annotation instanceof OneException) {
                    this.addException(clazz, (OneException) annotation);
                }
            }
        }
    }
    
    private void addException(Class<?> clazz, OneException exception) {
        if (!this.resultMap.containsKey(exception.result())) {
            this.logger.error(String.format("Invalid result in %s@%s", exception.toString(), clazz.getName()));
            return;
        }
        
        Class<? extends Throwable> exceptionClass = exception.exception();
        boolean overridden = false;
        for (OneException oldException : this.exceptionMap.values()) {
            if (oldException.exception().isAssignableFrom(exceptionClass)) {
                Class<?> oldClazz = this.exceptionClassMap.get(oldException);
                if (clazz.equals(oldClazz)) {
                    this.logger.error(String.format("%s: %s is overridden by %s", clazz.getName(),
                                                    exception.toString(), oldException.toString()));
                } else {
                    this.logger.debug(String.format("%s@%s is overridden by %s@%s", exception.toString(),
                                                    clazz.getName(), oldException.toString(),
                                                    oldClazz.getName()));
                }
                overridden = true;
                break;
            }
        }
        if (!overridden) {
            this.exceptionMap.put(exceptionClass, exception);
            this.exceptionClassMap.put(exception, clazz);
        }
    }
}
