
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.StringValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringValidatorAction;

public class PropertyDescriptorStringValidatorTest extends PropertyDescriptorTest {

    private int minLength;

    private int maxLength;

    private String pattern = "";

    @Before
    public void setUp() {
        super.setUp();
        this.minLength = 0;
        this.maxLength = Integer.MAX_VALUE;
    }

    public void check(String name) {
        this.name = name;
        PropertyDescriptor propertyDescriptor =
                this.getPropertyDescriptor(PropertyDescriptor.getInputProperties(StringValidatorAction.class));
        List<Annotation> annotationList = propertyDescriptor.getValidators();
        assertThat(annotationList.size(), is(1));
        assertThat(annotationList.get(0), is(StringValidator.class));
        StringValidator validator = (StringValidator) annotationList.get(0);
        assertThat(validator.minLength(), is(this.minLength));
        assertThat(validator.maxLength(), is(this.maxLength));
        assertThat(validator.pattern(), is(this.pattern));
    }

    @Test
    public void testStringValidatorForStringProperty() {
        this.check("stringProp");
    }

    @Test
    public void testStringValidatorForStringArrayProperty() {
        this.check("stringArrayProp");
    }

    @Test
    public void testStringValidatorMinLength() {
        this.minLength = 1;
        this.check("minLengthProp");
    }

    @Test
    public void testStringValidatorMaxLength() {
        this.maxLength = 2;
        this.check("maxLengthProp");
    }

    @Test
    public void testStringValidatorPattern() {
        this.pattern = "pattern";
        this.check("patternProp");
    }
}
