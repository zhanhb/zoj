
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.IntRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntValidatorAction;

public class PropertyDescriptorIntValidatorTest extends PropertyDescriptorTest {

    private int min;

    private int max;

    @Before
    public void setUp() {
        super.setUp();
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;
    }

    public void check(String name) {
        this.name = name;
        PropertyDescriptor propertyDescriptor =
                this
                    .getPropertyDescriptor(PropertyDescriptor
                                                             .getInputProperties(new MockActionDescriptor(
                                                                                                          IntValidatorAction.class)));
        List<Annotation> annotationList = propertyDescriptor.getValidators();
        assertThat(annotationList.size(), is(1));
        assertThat(annotationList.get(0), is(IntRangeValidator.class));
        IntRangeValidator validator = (IntRangeValidator) annotationList.get(0);
        assertThat(validator.min(), is(this.min));
        assertThat(validator.max(), is(this.max));
    }

    @Test
    public void testIntValidatorForIntProperty() {
        this.check("intProp");
    }

    @Test
    public void testIntValidatorForLongProperty() {
        this.check("longProp");
    }

    @Test
    public void testIntValidatorForIntArrayProperty() {
        this.check("intArrayProp");
    }

    @Test
    public void testIntValidatorForLongArrayProperty() {
        this.check("longArrayProp");
    }

    @Test
    public void testIntValidatorMin() {
        this.min = 1;
        this.check("minProp");
    }

    @Test
    public void testIntValidatorMax() {
        this.max = 2;
        this.check("maxProp");
    }

}
