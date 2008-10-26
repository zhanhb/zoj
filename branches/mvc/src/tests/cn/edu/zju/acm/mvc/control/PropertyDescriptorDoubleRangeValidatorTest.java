
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.DoubleRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.TestDoubleRangeValidatorAction;

public class PropertyDescriptorDoubleRangeValidatorTest extends PropertyDescriptorTest {

    private double min;

    private double max;

    @BeforeClass
    public static void init() {
        init(TestDoubleRangeValidatorAction.class);
        assertThat("Invalid number of input properties", inputPropertyList.size(), is(8));
    }

    @Before
    public void setUp() {
        super.setUp();
        this.min = Double.MIN_VALUE;
        this.max = Double.MAX_VALUE;
        this.conversionExceptionClasses.add(NumberFormatException.class);
    }

    public void check(String name) {
        this.name = name;
        this.validatorListSize = 1;
        PropertyDescriptor propertyDescriptor = this.getPropertyDescriptor(inputPropertyList);
        this.check(propertyDescriptor);
        Annotation annotation = propertyDescriptor.getValidators().get(0);
        assertThat(annotation, is(DoubleRangeValidator.class));
        DoubleRangeValidator validator = (DoubleRangeValidator) annotation;
        assertThat(validator.min(), is(this.min));
        assertThat(validator.max(), is(this.max));
    }

    @Test
    public void testNoneOnDouble() {
        this.type = this.rawType = double.class;
        this.check("noneOnDoubleProp");
    }

    @Test
    public void testMinOnDouble() {
        this.type = this.rawType = double.class;
        this.min = 1;
        this.check("minOnDoubleProp");
    }

    @Test
    public void testMaxOnDouble() {
        this.type = this.rawType = double.class;
        this.max = 2;
        this.check("maxOnDoubleProp");
    }

    @Test
    public void testMinAndMaxOnDouble() {
        this.type = this.rawType = double.class;
        this.min = 1;
        this.max = 2;
        this.check("minAndMaxOnDoubleProp");
    }

    @Test
    public void testNoneOnDoubleArray() {
        this.type = this.rawType = double[].class;
        this.componentType = double.class;
        this.check("noneOnDoubleArrayProp");
    }

    @Test
    public void testMinOnDoubleArray() {
        this.type = this.rawType = double[].class;
        this.componentType = double.class;
        this.min = 1;
        this.check("minOnDoubleArrayProp");
    }

    @Test
    public void testMaxOnDoubleArray() {
        this.type = this.rawType = double[].class;
        this.componentType = double.class;
        this.max = 2;
        this.check("maxOnDoubleArrayProp");
    }

    @Test
    public void testMinAndMaxOnDoubleArray() {
        this.type = this.rawType = double[].class;
        this.componentType = double.class;
        this.min = 1;
        this.max = 2;
        this.check("minAndMaxOnDoubleArrayProp");
    }
}
