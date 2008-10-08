
package cn.edu.zju.acm.mvc.control;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.edu.zju.acm.mvc.control.annotation.Exceptions;
import cn.edu.zju.acm.mvc.control.annotation.OneException;
import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.Results;
import cn.edu.zju.acm.onlinejudge.util.Pair;

public class ActionDescriptor {

    private Logger logger = Logger.getLogger(PropertyDescriptor.class);

    private Class<? extends Action> actionClass;

    private List<Pair<String, String>> resultList = new ArrayList<Pair<String, String>>();

    private List<Pair<Class<? extends Throwable>, String>> exceptionList =
            new ArrayList<Pair<Class<? extends Throwable>, String>>();

    private List<PropertyDescriptor> inputPropertyList;

    private List<PropertyDescriptor> outputPropertyList;

    public ActionDescriptor(Class<? extends Action> actionClass) {
        this.actionClass = actionClass;
        this.inputPropertyList = PropertyDescriptor.getInputProperties(actionClass);
        this.outputPropertyList = PropertyDescriptor.getOutputProperties(actionClass);
    }

    private void fillAnnotationLists(Class<?> clazz, List<Pair<Result, Class<?>>> resultList,
                                     List<Pair<OneException, Class<?>>> exceptionList) {
        if (!Action.class.equals(clazz)) {
            this.fillAnnotationLists(clazz.getSuperclass(), resultList, exceptionList);
        }
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation instanceof Exceptions) {
                for (OneException exception : ((Exceptions) annotation).value()) {
                    exceptionList.add(new Pair<OneException, Class<?>>(exception, clazz));
                }
            } else if (annotation instanceof OneException) {
                exceptionList.add(new Pair<OneException, Class<?>>((OneException) annotation, clazz));
            } else if (annotation instanceof Results) {
                for (Result result : ((Results) annotation).value()) {
                    resultList.add(new Pair<Result, Class<?>>(result, clazz));
                }
            } else if (annotation instanceof Result) {
                resultList.add(new Pair<Result, Class<?>>((Result) annotation, clazz));
            }
        }
    }

    public Class<? extends Action> getActionClass() {
        return this.actionClass;
    }

    public List<Pair<String, String>> getResultList() {
        return this.resultList;
    }

    public List<Pair<Class<? extends Throwable>, String>> getExceptionList() {
        return this.exceptionList;
    }

    public List<PropertyDescriptor> getInputPropertyList() {
        return this.inputPropertyList;
    }

    public List<PropertyDescriptor> getOutputPropertyList() {
        return this.outputPropertyList;
    }

    public void buildAnnotationLists(ResultFilter filter) {
        List<Pair<Result, Class<?>>> tempResultList = new ArrayList<Pair<Result, Class<?>>>();
        List<Pair<OneException, Class<?>>> tempExceptionList = new ArrayList<Pair<OneException, Class<?>>>();
        this.fillAnnotationLists(this.actionClass, tempResultList, tempExceptionList);
        Map<String, Pair<Result, Class<?>>> resultMap = new HashMap<String, Pair<Result, Class<?>>>();
        for (ListIterator<Pair<Result, Class<?>>> iter = tempResultList.listIterator(this.resultList.size() - 1); iter
                                                                                                                      .hasPrevious();) {
            Pair<Result, Class<?>> pair = iter.previous();
            Result result = pair.getFirst();
            Class<?> clazz = pair.getSecond();
            String resultName = result.name();
            String resultValue = result.value();
            if (filter.filter(resultValue)) {
                Pair<Result, Class<?>> oldPair = resultMap.get(resultName);
                if (oldPair == null) {
                    resultMap.put(resultName, pair);
                    this.resultList.add(new Pair<String, String>(resultName, resultValue));
                } else {
                    this.logger.debug(result + " " + clazz.getName() + " is overridden by " + oldPair.getFirst() + " " +
                        oldPair.getSecond().getName());
                }
            }
        }
        List<Pair<OneException, Class<?>>> finalExceptionList = new ArrayList<Pair<OneException, Class<?>>>();
        for (ListIterator<Pair<OneException, Class<?>>> iter =
                tempExceptionList.listIterator(this.resultList.size() - 1); iter.hasPrevious();) {
            Pair<OneException, Class<?>> pair = iter.previous();
            OneException exception = pair.getFirst();
            Class<?> clazz = pair.getSecond();
            if (resultMap.get(exception.result()) == null) {
                this.logger.error("Result undefined: " + exception + " " + clazz.getName());
            } else {
                Class<? extends Throwable> exceptionClass = exception.exception();
                boolean overridden = false;
                for (Pair<OneException, Class<?>> oldPair : finalExceptionList) {
                    if (exceptionClass.isAssignableFrom(oldPair.getFirst().exception())) {
                        this.logger.debug(exception + " " + clazz.getName() + " is overridden by " +
                            oldPair.getFirst() + " " + oldPair.getSecond().getName());
                        overridden = true;
                        break;
                    }
                }
                if (!overridden) {
                    finalExceptionList.add(pair);
                }
            }
        }
        for (Pair<OneException, Class<?>> pair : finalExceptionList) {
            OneException exception = pair.getFirst();
            this.exceptionList.add(new Pair<Class<? extends Throwable>, String>(exception.exception(),
                                                                                resultMap.get(exception.result())
                                                                                         .getFirst().value()));
        }
    }

}
