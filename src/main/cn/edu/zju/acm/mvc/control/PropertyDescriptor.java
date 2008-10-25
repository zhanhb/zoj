
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import cn.edu.zju.acm.mvc.control.annotation.ConversionError;
import cn.edu.zju.acm.mvc.control.annotation.Cookie;
import cn.edu.zju.acm.mvc.control.annotation.Session;
import cn.edu.zju.acm.mvc.control.annotation.validator.FloatRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.Required;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringPatternValidator;

public class PropertyDescriptor {

    private static Logger logger = Logger.getLogger(PropertyDescriptor.class);

    private ActionDescriptor actionDescriptor;

    private String name;

    private Type type;

    private Method accessMethod = null;

    private Required requiredAnnotation = null;

    private boolean sessionVariable = false;

    private Cookie cookieAnnotation = null;

    private List<Annotation> validators = new ArrayList<Annotation>();

    private Class<?> rawType;

    private Class<?> componentType;

    private List<Class<? extends Exception>> conversionExceptionClasses = new ArrayList<Class<? extends Exception>>();

    private String conversionErrorMessageKey = null;

    private PropertyDescriptor() {
    }

    public static List<PropertyDescriptor> getInputProperties(ActionDescriptor actionDescriptor) {
        return getProperties(actionDescriptor, true);
    }

    public static List<PropertyDescriptor> getOutputProperties(ActionDescriptor actionDescriptor) {
        return getProperties(actionDescriptor, false);
    }

    private static List<PropertyDescriptor> getProperties(ActionDescriptor actionDescriptor, boolean input) {
        List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
        for (Method method : actionDescriptor.getActionClass().getMethods()) {
            if ((method.getName().startsWith("set") && input || (method.getName().startsWith("get") || method
                                                                                                             .getName()
                                                                                                             .startsWith(
                                                                                                                         "is")) &&
                !input) &&
                !Object.class.equals(method.getDeclaringClass())) {
                Type propertyType = input ? determineTypeFromSetter(method) : determineTypeFromGetter(method);
                if (propertyType == null) {
                    continue;
                }
                String methodName = method.getName();
                String propertyName = methodName.charAt(0) == 'i' ? methodName.substring(2) : methodName.substring(3);
                if (propertyName.length() > 0) {
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor();
                    propertyDescriptor.name = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                    propertyDescriptor.actionDescriptor = actionDescriptor;
                    propertyDescriptor.accessMethod = method;
                    propertyDescriptor.type = propertyType;
                    propertyDescriptor.fillPropertyAttributes(input);
                    if (propertyDescriptor.sessionVariable || !input || canReadFromParameterValues(propertyType)) {
                        propertyDescriptorList.add(propertyDescriptor);
                    }
                }
            }
        }
        return propertyDescriptorList;
    }

    private static Type determineTypeFromGetter(Method getter) {
        Type[] parameterTypes = getter.getGenericParameterTypes();
        if (parameterTypes.length != 0) {
            return null;
        }
        Type type = getter.getGenericReturnType();
        return type;
    }

    private static Type determineTypeFromSetter(Method setter) {
        if (!void.class.equals(setter.getReturnType())) {
            return null;
        }
        Type[] parameterTypes = setter.getGenericParameterTypes();
        if (parameterTypes.length != 1) {
            return null;
        }
        Type type = parameterTypes[0];
        return type;
    }

    private static final Class<?>[] simplePropertyClasses =
            new Class<?>[] {int.class, long.class, double.class, boolean.class, String.class, Date.class, File.class};

    private static boolean isSimpleInputPropertyType(Type type) {
        for (Class<?> simplePropertyClass : simplePropertyClasses) {
            if (simplePropertyClass.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canReadFromParameterValues(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                Class<?> componentType = clazz.getComponentType();
                return !boolean[].class.equals(type) && isSimpleInputPropertyType(componentType) &&
                    !File.class.equals(componentType);
            }
            return isSimpleInputPropertyType(clazz);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type rawType = pType.getRawType();
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;
                if (rawClass.isInterface()) {
                    if (!Collection.class.equals(rawClass) && !List.class.equals(rawClass) &&
                        !Set.class.equals(rawClass)) {
                        return false;
                    }
                } else {
                    try {
                        rawClass.getConstructor();
                    } catch (SecurityException e) {
                        logger.debug(e);
                        return false;
                    } catch (NoSuchMethodException e) {
                        logger.debug(e);
                        return false;
                    }
                }
                Type[] typeArgs = pType.getActualTypeArguments();
                if (Collection.class.isAssignableFrom(rawClass)) {
                    return typeArgs.length == 1 && isSimpleInputPropertyType(typeArgs[0]) &&
                        !File.class.equals(typeArgs[0]);
                } else if (Map.class.isAssignableFrom(rawClass)) {
                    return typeArgs.length == 2 && isSimpleInputPropertyType(typeArgs[0]) &&
                        isSimpleInputPropertyType(typeArgs[1]) && !File.class.equals(typeArgs[0]);
                }
            }
        }
        return false;
    }

    private void fillPropertyAttributes(boolean input) {
        this.determineConversionExceptionClasses(this.type);
        this.determineTypes();
        for (Annotation annotation : this.accessMethod.getAnnotations()) {
            String error = "";
            if (annotation instanceof Required) {
                if (input) {
                    this.requiredAnnotation = (Required) annotation;
                } else {
                    error = "Required only applies to setters";
                }
            } else if (annotation instanceof IntRangeValidator) {
                if (!input) {
                    error = "IntRangeValidator only applies to setters";
                } else if (!this.isIntType(this.type) && !this.isIntType(this.componentType)) {
                    error = " IntRangeValidator only applies to int or int array properties";
                } else {
                    IntRangeValidator intRangeValidator = (IntRangeValidator) annotation;
                    if (intRangeValidator.max() < intRangeValidator.min()) {
                        error += " max should not be less than min";
                    }
                }
            } else if (annotation instanceof FloatRangeValidator) {
                if (!input) {
                    error = "FloatRangeValidator only applies to setters";
                } else if (!this.isFloatType(this.type) && !this.isFloatType(this.componentType)) {
                    error = " FloatRangeValidator only applies to double or double array properties";
                } else {
                    FloatRangeValidator floatRangeValidator = (FloatRangeValidator) annotation;
                    if (floatRangeValidator.max() < floatRangeValidator.min()) {
                        error += " max should not be less than min";
                    }
                }
            } else if (annotation instanceof StringLengthValidator) {
                if (!input) {
                    error = "StringLengthValidator only applies to setters";
                } else if (!String.class.equals(this.type) && !String.class.equals(this.componentType)) {
                    error = " StringLengthValidator only applies to String or String array properties";
                } else {
                    StringLengthValidator stringValidator = (StringLengthValidator) annotation;
                    if (stringValidator.minLength() < 0) {
                        error = " minLength should not be negative";
                    }
                    if (stringValidator.maxLength() < stringValidator.minLength()) {
                        error += " maxLength should not be less than minLength";
                    }
                }
            } else if (annotation instanceof StringPatternValidator) {
                if (!input) {
                    error = "StringPatternValidator only applies to setters";
                } else if (!String.class.equals(this.type) && !String.class.equals(this.componentType)) {
                    error = " StringPatternValidator only applies to String or String array properties";
                } else {
                    StringPatternValidator stringPatternValidator = (StringPatternValidator) annotation;
                    if (stringPatternValidator.pattern().length() > 0) {
                        try {
                            Pattern.compile(stringPatternValidator.pattern());
                        } catch (PatternSyntaxException e) {
                            error +=
                                    " Invalid pattern value '" + stringPatternValidator.pattern() + "' " +
                                        e.getMessage();
                        }
                    }
                }
            } else if (annotation instanceof Session) {
                this.sessionVariable = true;
            } else if (annotation instanceof Cookie) {
                if (canReadFromParameterValues(this.type)) {
                    this.cookieAnnotation = (Cookie) annotation;
                } else {
                    error = "Cookie does not apply to properties with type " + this.type;
                }
            } else if (annotation instanceof ConversionError) {
                if (!input) {
                    error = "ConversionError only applies to setters";
                } else if (String.class.equals(this.type) || String.class.equals(this.componentType)) {
                    error = " ConversionError does not apply to String or String array properties";
                } else {
                    this.conversionErrorMessageKey = ((ConversionError) annotation).message();
                }
            } else {
                continue;
            }
            if (error.length() == 0) {
                if (!(annotation instanceof Session || annotation instanceof Cookie)) {
                    this.validators.add(annotation);
                }
            } else {
                logger.error(String.format("Invalid %s for %s.%s:%s", annotation.annotationType().getSimpleName(),
                                           this.actionDescriptor.getActionClass().getName(),
                                           this.accessMethod.getName(), error));
            }
        }
        if (this.sessionVariable) {
            if (this.requiredAnnotation != null) {
                logger.error(String.format("Session variables should not have Required annotation. Location: %s.%s",
                                           this.actionDescriptor.getActionClass().getName(),
                                           this.accessMethod.getName()));
                this.requiredAnnotation = null;
            }
            if (this.validators.size() > 0) {
                logger.error(String.format("Session variables should not have any validator. Location: %s.%s",
                                           this.actionDescriptor.getActionClass().getName(),
                                           this.accessMethod.getName()));
                this.validators.clear();
            }
            if (this.cookieAnnotation != null) {
                logger.error(String.format("Session variables should not have Cookie annotation. Location: %s.%s",
                                           this.actionDescriptor.getActionClass().getName(),
                                           this.accessMethod.getName()));
                this.cookieAnnotation = null;
            }
        }
    }

    private void determineConversionExceptionClasses(Type type) {
        if (type instanceof ParameterizedType) {
            this.determineConversionExceptionClasses(((ParameterizedType) type).getActualTypeArguments()[0]);
        } else {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                this.determineConversionExceptionClasses(clazz.getComponentType());
            } else if (File.class.equals(type)) {
                // TODO
            } else if (Date.class.equals(type)) {
                this.conversionExceptionClasses.add(ParseException.class);
            } else if (String.class.equals(type) || boolean.class.equals(type) || Boolean.class.equals(type)) {
                // No exception
            } else {
                // Should be number
                this.conversionExceptionClasses.add(NumberFormatException.class);
            }
        }
    }

    private void determineTypes() {
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            this.rawType = (Class<?>) pType.getRawType();
            this.componentType = (Class<?>) pType.getActualTypeArguments()[0];
        } else {
            this.rawType = (Class<?>) type;
            if (this.rawType.isArray()) {
                this.componentType = this.rawType.getComponentType();
            }
        }
    }

    private boolean isIntType(Type type) {
        return int.class.equals(type) || long.class.equals(type);
    }

    private boolean isFloatType(Type type) {
        return double.class.equals(type);
    }

    public ActionDescriptor getActionDescriptor() {
        return this.actionDescriptor;
    }

    public String getName() {
        return this.name;
    }

    public Type getType() {
        return this.type;
    }

    public Method getAccessMethod() {
        return this.accessMethod;
    }

    public Required getRequiredAnnotation() {
        return this.requiredAnnotation;
    }

    public boolean isSessionVariable() {
        return this.sessionVariable;
    }

    public Cookie getCookieAnnotation() {
        return this.cookieAnnotation;
    }

    public List<Annotation> getValidators() {
        return this.validators;
    }

    public Class<?> getRawType() {
        return this.rawType;
    }

    public Class<?> getComponentType() {
        return this.componentType;
    }

    public List<Class<? extends Exception>> getConversionExceptionClasses() {
        return this.conversionExceptionClasses;
    }

    public String getConversionErrorMessageKey() {
        if (!this.accessMethod.getName().startsWith("set") || String.class.equals(this.type) ||
            String.class.equals(this.componentType)) {
            return null;
        }
        return this.conversionErrorMessageKey == null ? this.actionDescriptor.getConversionErrorMessageKey()
                                                     : this.conversionErrorMessageKey;
    }
}
