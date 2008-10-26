
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.StringPatternValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.TestStringPatternValidatorAction;

public class PropertyDescriptorStringPatternValidatorTest extends PropertyDescriptorTest {

    private String pattern;

    @BeforeClass
    public static void init() {
        init(TestStringPatternValidatorAction.class);
        assertThat("Invalid number of input properties", inputPropertyList.size(), is(2));
    }

    @Before
    public void setUp() {
        super.setUp();
        this.pattern = null;
    }

    public void check(String name) {
        this.name = name;
        this.validatorListSize = 1;
        PropertyDescriptor propertyDescriptor = this.getPropertyDescriptor(inputPropertyList);
        this.check(propertyDescriptor);
        Annotation annotation =propertyDescriptor.getValidators().get(0);
        assertThat(annotation, is(StringPatternValidator.class));
        StringPatternValidator validator = (StringPatternValidator) annotation;
        assertThat(validator.pattern(), is(this.pattern));
    }

    @Test
    public void testString() {
        this.type = this.rawType = String.class;
        this.pattern = "a*";
        this.check("stringProp");
    }
    
    @Test
    public void testStringArray() {
        this.type = this.rawType = String[].class;
        this.componentType = String.class;
        this.pattern = "a*";
        this.check("stringArrayProp");
    }
}
