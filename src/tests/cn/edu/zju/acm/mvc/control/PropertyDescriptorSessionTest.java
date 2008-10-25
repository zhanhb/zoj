
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.TestInvalidSessionPropertyAction;
import cn.edu.zju.acm.mvc.control.annotation.TestSessionPropertyAction;

public class PropertyDescriptorSessionTest extends PropertyDescriptorTest {

    @Test
    public void testInput() {
        List<PropertyDescriptor> inputProperties =
                PropertyDescriptor.getInputProperties(new MockActionDescriptor(TestSessionPropertyAction.class));
        assertThat("Invalid number of input properties", inputProperties.size(), is(5));
        for (PropertyDescriptor propertyDescriptor : inputProperties) {
            assertThat(propertyDescriptor.isSessionVariable(), is(true));
        }
    }

    @Test
    public void testOutput() {
        List<PropertyDescriptor> outputProperties =
                PropertyDescriptor.getOutputProperties(new MockActionDescriptor(TestSessionPropertyAction.class));
        assertThat("Invalid number of output properties", outputProperties.size(), is(6));
        for (PropertyDescriptor propertyDescriptor : outputProperties) {
            if (!propertyDescriptor.getName().equals("fieldErrors")) {
                assertThat(propertyDescriptor.isSessionVariable(), is(true));
            }
        }
    }

    @Test
    public void testInvalidInput() {
        List<PropertyDescriptor> inputProperties =
                PropertyDescriptor.getInputProperties(new MockActionDescriptor(TestInvalidSessionPropertyAction.class));
        assertThat("Invalid number of input properties", inputProperties.size(), is(5));
        for (PropertyDescriptor propertyDescriptor : inputProperties) {
            assertThat(propertyDescriptor.isSessionVariable(), is(true));
            assertThat(propertyDescriptor.getRequiredAnnotation(), nullValue());
            assertThat(propertyDescriptor.getValidators().size(), is(0));
            assertThat(propertyDescriptor.getCookieAnnotation(), nullValue());
        }
    }

    @Test
    public void testInvalidOutput() {
        List<PropertyDescriptor> outputProperties =
                PropertyDescriptor.getOutputProperties(new MockActionDescriptor(TestInvalidSessionPropertyAction.class));
        assertThat("Invalid number of output properties", outputProperties.size(), is(6));
        for (PropertyDescriptor propertyDescriptor : outputProperties) {
            if (!propertyDescriptor.getName().equals("fieldErrors")) {
                assertThat(propertyDescriptor.isSessionVariable(), is(true));
                assertThat(propertyDescriptor.getRequiredAnnotation(), nullValue());
                assertThat(propertyDescriptor.getValidators().size(), is(0));
                assertThat(propertyDescriptor.getCookieAnnotation(), nullValue());
            }
        }
    }
}
