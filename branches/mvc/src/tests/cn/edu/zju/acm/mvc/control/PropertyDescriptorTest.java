
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.core.IsEqual;
import org.junit.Before;

public class PropertyDescriptorTest {

    protected static MockActionDescriptor actionDescriptor;

    protected static List<PropertyDescriptor> inputPropertyList;

    protected static List<PropertyDescriptor> outputPropertyList;

    protected String name;

    protected Type type;

    protected boolean required;

    protected boolean sessionVariable;

    protected boolean cookie;

    protected int validatorListSize;

    protected Class<?> rawType;

    protected Class<?> componentType;

    protected List<Class<? extends Exception>> conversionExceptionClasses;

    protected String conversionErrorMessageKey;

    public static void init(Class<? extends Action> actionClass) {
        actionDescriptor = new MockActionDescriptor(actionClass);
        inputPropertyList = PropertyDescriptor.getInputProperties(actionDescriptor);
        outputPropertyList = PropertyDescriptor.getOutputProperties(actionDescriptor);
    }

    @Before
    public void setUp() {
        this.name = null;
        this.type = null;
        this.required = false;
        this.sessionVariable = false;
        this.cookie = false;
        this.validatorListSize = 0;
        this.rawType = null;
        this.componentType = null;
        this.conversionExceptionClasses = new ArrayList<Class<? extends Exception>>();
        this.conversionErrorMessageKey = null;
    }

    protected PropertyDescriptor getPropertyDescriptor(List<PropertyDescriptor> propertyDescriptorList) {
        Map<String, PropertyDescriptor> propertyDescriptorMap = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptorList) {
            propertyDescriptorMap.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        PropertyDescriptor propertyDescriptor = propertyDescriptorMap.get(this.name);
        assertThat(String.format("Property '%s' is not available", this.name), propertyDescriptor, notNullValue());
        return propertyDescriptor;
    }

    protected void check(PropertyDescriptor propertyDescriptor) {
        assertThat(propertyDescriptor, notNullValue());
        assertThat("Invalid actionDescriptor", propertyDescriptor.getActionDescriptor(),
                   is(new IsEqual<ActionDescriptor>(actionDescriptor)));
        assertThat("Invalid name", propertyDescriptor.getName(), is(this.name));
        assertThat("Invalid type", propertyDescriptor.getType(), is(new IsEqual<Type>(this.type)));
        assertThat("Invalid requiredAnnotation", propertyDescriptor.getRequiredAnnotation(),
                   is(this.required ? notNullValue() : nullValue()));
        assertThat("Invalid sessionVariable", propertyDescriptor.isSessionVariable(), is(this.sessionVariable));
        assertThat("Invalid cookieAnnotation", propertyDescriptor.getCookieAnnotation(),
                   is(this.cookie ? notNullValue() : nullValue()));
        assertThat("Invalid raw type", propertyDescriptor.getRawType(), is(new IsEqual<Class<?>>(this.rawType)));
        assertThat("Invalid component type", propertyDescriptor.getComponentType(),
                   is(new IsEqual<Class<?>>(this.componentType)));
        assertThat("Invalid access method", propertyDescriptor.getAccessMethod(), notNullValue());
        assertThat("Invalid possibleExceptionClass", propertyDescriptor.getConversionExceptionClasses(),
                   is(new IsEqual<List<Class<? extends Exception>>>(this.conversionExceptionClasses)));
        assertThat("Invalid validators", propertyDescriptor.getValidators().size(), is(this.validatorListSize));
        assertThat("Invalid conversionErrorMessageKey", propertyDescriptor.getConversionErrorMessageKey(),
                   is(this.conversionErrorMessageKey));
    }
}
