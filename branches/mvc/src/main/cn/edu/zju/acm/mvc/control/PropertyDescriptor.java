
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.beanutils.ConversionException;
import org.apache.log4j.Logger;

import cn.edu.zju.acm.mvc.control.annotation.validator.DateValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.FloatValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.Required;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringValidator;

public class PropertyDescriptor {

    private static Logger logger = Logger.getLogger(PropertyDescriptor.class);

    private Class<? extends Action> owner;

    private String name;

    private Type type;

    private Method accessMethod = null;

    private boolean required = false;

    private List<Annotation> validators = new ArrayList<Annotation>();

    private Class<?> rawType;

    private Class<?> componentType;

    private List<Class<? extends Exception>> conversionExceptionClasses = new ArrayList<Class<? extends Exception>>();

    private PropertyDescriptor() {
    }

    public static List<PropertyDescriptor> getInputProperties(Class<? extends Action> actionClass) {
        return getProperties(actionClass, true);
    }

    public static List<PropertyDescriptor> getOutputProperties(Class<? extends Action> actionClass) {
        return getProperties(actionClass, false);
    }

    private static List<PropertyDescriptor> getProperties(Class<? extends Action> actionClass, boolean input) {
        List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
        for (Method method : actionClass.getMethods()) {
            if ((method.getName().startsWith("set") && input || method.getName().startsWith("get") && !input) &&
                !Object.class.equals(method.getDeclaringClass())) {
                Type propertyType = input ? determineTypeFromSetter(method) : determineTypeFromGetter(method);
                if (propertyType == null) {
                    continue;
                }
                String propertyName = method.getName().substring(3);
                if (propertyName.length() > 0) {
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor();
                    propertyDescriptor.name = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                    propertyDescriptor.owner = actionClass;
                    propertyDescriptor.accessMethod = method;
                    propertyDescriptor.type = propertyType;
                    if (input) {
                        propertyDescriptor.fillInputPropertyAttributes();
                    }
                    propertyDescriptorList.add(propertyDescriptor);
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
        if (isValidOutputPropertyType(type)) {
            return type;
        }
        return null;
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
        if (isValidInputPropertyType(type)) {
            return type;
        }
        return null;
    }

    private static final Class<?>[] simplePropertyClasses =
            new Class<?>[] {int.class, long.class, double.class, boolean.class, Integer.class, Long.class,
                            Double.class, BigInteger.class, BigDecimal.class, Boolean.class, String.class, Date.class,
                            File.class};

    private static boolean isSimpleInputPropertyType(Type type) {
        for (Class<?> simplePropertyClass : simplePropertyClasses) {
            if (simplePropertyClass.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidInputPropertyType(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                Class<?> componentType = clazz.getComponentType();
                return isSimpleInputPropertyType(componentType) && !File.class.equals(componentType);
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

    private static boolean isValidOutputPropertyType(Type type) {
        if (type instanceof Class) {
            return true;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type rawType = pType.getRawType();
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;
                Type[] typeArgs = pType.getActualTypeArguments();
                if (Collection.class.isAssignableFrom(rawClass)) {
                    return typeArgs.length == 1 && isValidOutputPropertyType(typeArgs[0]);
                } else if (Map.class.isAssignableFrom(rawClass)) {
                    return typeArgs.length == 2 && isValidOutputPropertyType(typeArgs[0]) &&
                        isValidOutputPropertyType(typeArgs[1]);
                }
            }
        }
        return false;
    }

    private void fillInputPropertyAttributes() {
        this.determineConversionExceptionClasses(this.type);
        this.determineTypes();
        for (Annotation annotation : this.accessMethod.getAnnotations()) {
            String error = "";
            if (annotation instanceof Required) {
                this.required = true;
            } else if (annotation instanceof IntValidator) {
                if (!this.isIntType(this.type) && !this.isIntType(this.componentType)) {
                    error = " IntValidator only appiles to int or int array properties";
                } else {
                    IntValidator intValidator = (IntValidator) annotation;
                    if (intValidator.max() < intValidator.min()) {
                        error += " max should not be less than min";
                    }
                }
            } else if (annotation instanceof FloatValidator) {
                if (!this.isFloatType(this.type) && !this.isFloatType(this.componentType)) {
                    error = " FloatValidator only appiles to double or double array properties";
                } else {
                    FloatValidator floatValidator = (FloatValidator) annotation;
                    if (floatValidator.max() < floatValidator.min()) {
                        error += " max should not be less than min";
                    }
                }
            } else if (annotation instanceof StringValidator) {
                if (!String.class.equals(this.type) && !String.class.equals(this.componentType)) {
                    error = " StringValidator only appiles to String or String array properties";
                } else {
                    StringValidator stringValidator = (StringValidator) annotation;
                    if (stringValidator.minLength() < 0) {
                        error = " minLength should not be negative";
                    }
                    if (stringValidator.maxLength() < stringValidator.minLength()) {
                        error += " maxLength should not be less than minLength";
                    }
                    if (stringValidator.pattern().length() > 0) {
                        try {
                            Pattern.compile(stringValidator.pattern());
                        } catch (PatternSyntaxException e) {
                            error += " Invalid pattern value '" + stringValidator.pattern() + "' " + e.getMessage();
                        }
                    }
                }
            } else if (annotation instanceof DateValidator) {
                if (!Date.class.equals(this.type) && !Date.class.equals(this.componentType)) {
                    error = " DateValidator only appiles to Date or Date array properties";
                } else {
                    DateValidator dateValidator = (DateValidator) annotation;
                    try {
                        SimpleDateFormat fmt = new SimpleDateFormat(dateValidator.format());
                        Date min = null;
                        Date max = null;
                        if (dateValidator.min().length() > 0) {
                            try {
                                min = fmt.parse(dateValidator.min());
                            } catch (ParseException e) {
                                error += " Invalid min value '" + dateValidator.min() + "'";
                            }
                        }
                        if (dateValidator.max().length() > 0) {
                            try {
                                max = fmt.parse(dateValidator.max());
                            } catch (ParseException e) {
                                error += " Invalid max value '" + dateValidator.max() + "'";
                            }
                        }
                        if (min != null && max != null && max.before(min)) {
                            error += " max should not be before min";
                        }
                    } catch (IllegalArgumentException e) {
                        error = " Invalid format value '" + dateValidator.format() + "' " + e.getMessage();
                    }
                }
            } else {
                continue;
            }
            if (error.length() == 0) {
                this.validators.add(annotation);
            } else {
                logger.error(String.format("Invalid %s for %s.%s:%s", annotation.annotationType().getSimpleName(),
                                           this.owner.getName(), this.accessMethod.getName(), error));
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
                this.conversionExceptionClasses.add(ConversionException.class);
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

    public Class<?> getOwner() {
        return this.owner;
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

    public boolean isRequired() {
        return this.required;
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
}
