
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.DateValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.DateValidatorAction;

public class PropertyDescriptorDateValidatorTest extends PropertyDescriptorTest {

    private String min;

    private String max;
    
    private String format;

    @Before
    public void setUp() {
        super.setUp();
        this.min = "";
        this.max = "";
        this.format = "yyyy-MM-dd";
    }

    public void check(String name) {
        this.name = name;
        PropertyDescriptor propertyDescriptor =
                this.getPropertyDescriptor(PropertyDescriptor.getInputProperties(DateValidatorAction.class));
        List<Annotation> annotationList = propertyDescriptor.getValidators();
        assertThat(annotationList.size(), is(1));
        assertThat(annotationList.get(0), is(DateValidator.class));
        DateValidator validator = (DateValidator) annotationList.get(0);
        assertThat(validator.min(), is(this.min));
        assertThat(validator.max(), is(this.max));
        assertThat(validator.format(), is(this.format));
    }

    @Test
    public void testDateValidatorForDateProperty() {
        this.check("dateProp");
    }

    @Test
    public void testDateValidatorForDateArrayProperty() {
        this.check("dateArrayProp");
    }

    @Test
    public void testDateValidatorMin() {
        this.min = "2000-01-01";
        this.check("minProp");
    }

    @Test
    public void testDateValidatorMax() {
        this.max = "2000-01-01";
        this.check("maxProp");
    }
}
