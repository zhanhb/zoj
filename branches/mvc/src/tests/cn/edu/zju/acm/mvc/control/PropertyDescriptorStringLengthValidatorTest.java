
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidatorAction;

public class PropertyDescriptorStringLengthValidatorTest extends PropertyDescriptorTest {

    private int minLength;

    private int maxLength;

    @Before
    public void setUp() {
        super.setUp();
        this.minLength = 0;
        this.maxLength = Integer.MAX_VALUE;
    }

    public void check(String name) {
        this.name = name;
        PropertyDescriptor propertyDescriptor =
                this
                    .getPropertyDescriptor(PropertyDescriptor
                                                             .getInputProperties(new MockActionDescriptor(
                                                                                                          StringLengthValidatorAction.class)));
        List<Annotation> annotationList = propertyDescriptor.getValidators();
        assertThat(annotationList.size(), is(1));
        assertThat(annotationList.get(0), is(StringLengthValidator.class));
        StringLengthValidator validator = (StringLengthValidator) annotationList.get(0);
        assertThat(validator.minLength(), is(this.minLength));
        assertThat(validator.maxLength(), is(this.maxLength));
    }

    @Test
    public void testStringLengthValidatorForStringProperty() {
        this.check("stringProp");
    }

    @Test
    public void testStringLengthValidatorForStringArrayProperty() {
        this.check("stringArrayProp");
    }

    @Test
    public void testStringLengthValidatorMinLength() {
        this.minLength = 1;
        this.check("minLengthProp");
    }

    @Test
    public void testStringLengthValidatorMaxLength() {
        this.maxLength = 2;
        this.check("maxLengthProp");
    }
}
