
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.StringPatternValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringPatternValidatorAction;

public class PropertyDescriptorStringPatternValidatorTest extends PropertyDescriptorTest {

    private String pattern = "";

    @Before
    public void setUp() {
        super.setUp();
    }

    public void check(String name) {
        this.name = name;
        PropertyDescriptor propertyDescriptor =
                this
                    .getPropertyDescriptor(PropertyDescriptor
                                                             .getInputProperties(new MockActionDescriptor(
                                                                                                          StringPatternValidatorAction.class)));
        List<Annotation> annotationList = propertyDescriptor.getValidators();
        assertThat(annotationList.size(), is(1));
        assertThat(annotationList.get(0), is(StringPatternValidator.class));
        StringPatternValidator validator = (StringPatternValidator) annotationList.get(0);
        assertThat(validator.pattern(), is(this.pattern));
    }

    @Test
    public void testStringValidatorPattern() {
        this.pattern = "pattern";
        this.check("patternProp");
    }
}
