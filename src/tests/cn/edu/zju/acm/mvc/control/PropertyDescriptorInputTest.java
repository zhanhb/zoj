
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.ConversionException;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;


public class PropertyDescriptorInputTest extends PropertyDescriptorTest {

    protected boolean required;

    protected List<Annotation> validators;

    protected Class<?> rawType;

    protected Class<?> componentType;

    protected List<Class<? extends Exception>> conversionExceptionClasses;

    @Before
    public void setUp() {
        super.setUp();
        this.required = false;
        this.validators = new ArrayList<Annotation>();
        this.rawType = null;
        this.componentType = null;
        this.conversionExceptionClasses = new ArrayList<Class<? extends Exception>>();
        this.owner = InputPropertyAction.class;
    }

    protected void check() {
        List<PropertyDescriptor> propertyDescriptorList = PropertyDescriptor.getInputProperties(this.owner);
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

    @Test
    public void testInt() {
        this.type = this.rawType = int.class;
        this.name = "intProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testLong() {
        this.type = this.rawType = long.class;
        this.name = "longProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testDouble() {
        this.type = this.rawType = double.class;
        this.name = "doubleProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testIntArray() {
        this.type = this.rawType = int[].class;
        this.componentType = int.class;
        this.name = "intArrayProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testLongArray() {
        this.type = this.rawType = long[].class;
        this.componentType = long.class;
        this.name = "longArrayProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testDoubleArray() {
        this.type = this.rawType = double[].class;
        this.componentType = double.class;
        this.name = "doubleArrayProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testBoolean() {
        this.type = this.rawType = boolean.class;
        this.name = "booleanProp";
        this.check();
    }

    @Test
    public void testString() {
        this.type = this.rawType = String.class;
        this.name = "stringProp";
        this.check();
    }

    @Test
    public void testDate() {
        this.type = this.rawType = Date.class;
        this.name = "dateProp";
        this.conversionExceptionClasses.add(ConversionException.class);
        this.check();
    }

    @Test
    public void testFile() {
        this.type = this.rawType = File.class;
        this.name = "fileProp";
        // this.conversionExceptionClasses = new Class[] {ConversionException.class};
        this.check();
    }

    @Test
    public void testBooleanArray() {
        this.type = this.rawType = boolean[].class;
        this.componentType = boolean.class;
        this.name = "booleanArrayProp";
        this.check();
    }

    @Test
    public void testStringArray() {
        this.type = this.rawType = String[].class;
        this.componentType = String.class;
        this.name = "stringArrayProp";
        this.check();
    }

    @Test
    public void testDateArray() {
        this.type = this.rawType = Date[].class;
        this.componentType = Date.class;
        this.name = "dateArrayProp";
        this.conversionExceptionClasses.add(ConversionException.class);
        this.check();
    }
}
