
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.core.IsEqual;

public class PropertyDescriptorTest {

    protected Class<? extends Action> owner;

    protected String name;

    protected Type type;

    protected boolean required;

    protected List<Annotation> validators;

    protected Class<?> rawType;

    protected Class<?> componentType;

    protected List<Class<? extends Exception>> conversionExceptionClasses;

    public void setUp() {
        this.owner = null;
        this.name = null;
        this.type = null;
        this.required = false;
        this.validators = new ArrayList<Annotation>();
        this.rawType = null;
        this.componentType = null;
        this.conversionExceptionClasses = new ArrayList<Class<? extends Exception>>();
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

    protected void check(List<PropertyDescriptor> propertyDescriptorList) {
        PropertyDescriptor propertyDescriptor = this.getPropertyDescriptor(propertyDescriptorList);
        assertThat("Invalid raw type", propertyDescriptor.getRawType(), is(new IsEqual<Class<?>>(this.rawType)));
        assertThat("Invalid component type", propertyDescriptor.getComponentType(),
                   is(new IsEqual<Class<?>>(this.componentType)));
        assertThat("Invalid access method", propertyDescriptor.getAccessMethod(), notNullValue());
        assertThat("Invalid name", propertyDescriptor.getName(), is(this.name));
        assertThat("Invalid owner", propertyDescriptor.getOwner(), is(new IsEqual<Class<?>>(this.owner)));
        assertThat("Invalid possibleExceptionClass", propertyDescriptor.getConversionExceptionClasses(),
                   is(new IsEqual<List<Class<? extends Exception>>>(this.conversionExceptionClasses)));
        assertThat("Invalid type", propertyDescriptor.getType(), is(new IsEqual<Type>(this.type)));
        assertThat("Invalid validators", propertyDescriptor.getValidators(), is(this.validators));
        assertThat("Invalid required", propertyDescriptor.isRequired(), is(this.required));
    }
}
