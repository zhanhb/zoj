
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.FloatValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.FloatValidatorAction;

public class PropertyDescriptorFloatValidatorTest extends PropertyDescriptorTest {

    private double min;

    private double max;

    @Before
    public void setUp() {
        super.setUp();
        this.min = Double.MIN_VALUE;
        this.max = Double.MAX_VALUE;
    }

    public void check(String name) {
        this.name = name;
        PropertyDescriptor propertyDescriptor =
                this.getPropertyDescriptor(PropertyDescriptor.getInputProperties(FloatValidatorAction.class));
        List<Annotation> annotationList = propertyDescriptor.getValidators();
        assertThat(annotationList.size(), is(1));
        assertThat(annotationList.get(0), is(FloatValidator.class));
        FloatValidator validator = (FloatValidator) annotationList.get(0);
        assertThat(validator.min(), is(this.min));
        assertThat(validator.max(), is(this.max));
    }

    @Test
    public void testFloatValidatorForDoubleProperty() {
        this.check("doubleProp");
    }

    @Test
    public void testFloatValidatorForDoubleArrayProperty() {
        this.check("doubleArrayProp");
    }

    @Test
    public void testFloatValidatorMin() {
        this.min = 1.0;
        this.check("minProp");
    }

    @Test
    public void testFloatValidatorMax() {
        this.max = 2.0;
        this.check("maxProp");
    }
}
