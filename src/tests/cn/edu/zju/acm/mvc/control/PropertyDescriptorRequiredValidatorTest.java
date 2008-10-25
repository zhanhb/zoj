
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.Required;
import cn.edu.zju.acm.mvc.control.annotation.validator.RequiredAction;

public class PropertyDescriptorRequiredValidatorTest extends PropertyDescriptorTest {

    @Test
    public void test() {
        List<PropertyDescriptor> propertyDescriptorList =
                PropertyDescriptor.getInputProperties(new MockActionDescriptor(RequiredAction.class));
        assertThat(propertyDescriptorList.size(), is(11));
        for (PropertyDescriptor propertyDescriptor : propertyDescriptorList) {
            List<Annotation> annotationList = propertyDescriptor.getValidators();
            assertThat(annotationList.size(), is(1));
            assertThat(annotationList.get(0), is(Required.class));
            assertThat(propertyDescriptor.getRequiredAnnotation(), notNullValue());
        }
    }
}
