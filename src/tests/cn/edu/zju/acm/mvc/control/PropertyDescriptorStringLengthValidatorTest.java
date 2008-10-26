
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.TestStringLengthValidatorAction;

public class PropertyDescriptorStringLengthValidatorTest extends PropertyDescriptorTest {

    private int min;

    private int max;

    @BeforeClass
    public static void init() {
        init(TestStringLengthValidatorAction.class);
        assertThat("Invalid number of input properties", inputPropertyList.size(), is(8));
    }

    @Before
    public void setUp() {
        super.setUp();
        this.min = 0;
        this.max = Integer.MAX_VALUE;
    }

    public void check(String name) {
        this.name = name;
        this.validatorListSize = 1;
        PropertyDescriptor propertyDescriptor = this.getPropertyDescriptor(inputPropertyList);
        this.check(propertyDescriptor);
        Annotation annotation = propertyDescriptor.getValidators().get(0);
        assertThat(annotation, is(StringLengthValidator.class));
        StringLengthValidator validator = (StringLengthValidator) annotation;
        assertThat(validator.min(), is(this.min));
        assertThat(validator.max(), is(this.max));
    }

    @Test
    public void testNoneOnString() {
        this.type = this.rawType = String.class;
        this.check("noneOnStringProp");
    }

    @Test
    public void testMinOnString() {
        this.type = this.rawType = String.class;
        this.min = 1;
        this.check("minOnStringProp");
    }

    @Test
    public void testMaxOnString() {
        this.type = this.rawType = String.class;
        this.max = 2;
        this.check("maxOnStringProp");
    }

    @Test
    public void testMinAndMaxOnString() {
        this.type = this.rawType = String.class;
        this.min = 1;
        this.max = 2;
        this.check("minAndMaxOnStringProp");
    }

    @Test
    public void testNoneOnStringArray() {
        this.type = this.rawType = String[].class;
        this.componentType = String.class;
        this.check("noneOnStringArrayProp");
    }

    @Test
    public void testMinOnStringArray() {
        this.type = this.rawType = String[].class;
        this.componentType = String.class;
        this.min = 1;
        this.check("minOnStringArrayProp");
    }

    @Test
    public void testMaxOnStringArray() {
        this.type = this.rawType = String[].class;
        this.componentType = String.class;
        this.max = 2;
        this.check("maxOnStringArrayProp");
    }

    @Test
    public void testMinAndMaxOnStringArray() {
        this.type = this.rawType = String[].class;
        this.componentType = String.class;
        this.min = 1;
        this.max = 2;
        this.check("minAndMaxOnStringArrayProp");
    }
}
