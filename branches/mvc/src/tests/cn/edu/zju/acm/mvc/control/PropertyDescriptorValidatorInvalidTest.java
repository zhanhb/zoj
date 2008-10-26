
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.ValidatorInvalidAction;

public class PropertyDescriptorValidatorInvalidTest extends PropertyDescriptorTest {

    @Test
    public void test() {
        List<PropertyDescriptor> propertyDescriptorList =
                PropertyDescriptor.getInputProperties(new MockActionDescriptor(ValidatorInvalidAction.class));
        assertThat(propertyDescriptorList.size(), is(9));
        for (PropertyDescriptor propertyDescriptor : propertyDescriptorList) {
            List<Annotation> annotationList = propertyDescriptor.getValidators();
            assertThat(annotationList.size(), is(0));
        }
    }
}
