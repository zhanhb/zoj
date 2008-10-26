
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.IntRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.TestIntRangeValidatorAction;

public class PropertyDescriptorIntRangeValidatorTest extends PropertyDescriptorTest {

    private int min;

    private int max;

    @BeforeClass
    public static void init() {
        init(TestIntRangeValidatorAction.class);
        assertThat("Invalid number of input properties", inputPropertyList.size(), is(8));
    }

    @Before
    public void setUp() {
        super.setUp();
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;
        this.conversionExceptionClasses.add(NumberFormatException.class);
    }

    public void check(String name) {
        this.name = name;
        this.validatorListSize = 1;
        PropertyDescriptor propertyDescriptor = this.getPropertyDescriptor(inputPropertyList);
        this.check(propertyDescriptor);
        Annotation annotation =propertyDescriptor.getValidators().get(0);
        assertThat(annotation, is(IntRangeValidator.class));
        IntRangeValidator validator = (IntRangeValidator) annotation;
        assertThat(validator.min(), is(this.min));
        assertThat(validator.max(), is(this.max));
    }

    @Test
    public void testNoneOnInt() {
        this.type = this.rawType = int.class;
        this.check("noneOnIntProp");
    }

    @Test
    public void testMinOnInt() {
        this.type = this.rawType = int.class;
        this.min = 1;
        this.check("minOnIntProp");
    }

    @Test
    public void testMaxOnInt() {
        this.type = this.rawType = int.class;
        this.max = 2;
        this.check("maxOnIntProp");
    }

    @Test
    public void testMinAndMaxOnInt() {
        this.type = this.rawType = int.class;
        this.min = 1;
        this.max = 2;
        this.check("minAndMaxOnIntProp");
    }

    @Test
    public void testNoneOnIntArray() {
        this.type = this.rawType = int[].class;
        this.componentType = int.class;
        this.check("noneOnIntArrayProp");
    }

    @Test
    public void testMinOnIntArray() {
        this.type = this.rawType = int[].class;
        this.componentType = int.class;
        this.min = 1;
        this.check("minOnIntArrayProp");
    }

    @Test
    public void testMaxOnIntArray() {
        this.type = this.rawType = int[].class;
        this.componentType = int.class;
        this.max = 2;
        this.check("maxOnIntArrayProp");
    }

    @Test
    public void testMinAndMaxOnIntArray() {
        this.type = this.rawType = int[].class;
        this.componentType = int.class;
        this.min = 1;
        this.max = 2;
        this.check("minAndMaxOnIntArrayProp");
    }
}
