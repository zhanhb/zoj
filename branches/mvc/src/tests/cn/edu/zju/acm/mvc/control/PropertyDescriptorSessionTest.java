
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

public class PropertyDescriptorSessionTest extends PropertyDescriptorTest {

    @Test
    public void testInput() {
        List<PropertyDescriptor> inputProperties =
                PropertyDescriptor.getInputProperties(TestSessionPropertyAction.class);
        assertThat("Invalid number of input properties", inputProperties.size(), is(5));
        for (PropertyDescriptor propertyDescriptor : inputProperties) {
            assertThat(propertyDescriptor.isSessionVariable(), is(true));
        }
    }

    @Test
    public void testOutput() {
        List<PropertyDescriptor> outputProperties =
                PropertyDescriptor.getOutputProperties(TestSessionPropertyAction.class);
        assertThat("Invalid number of output properties", outputProperties.size(), is(6));
        for (PropertyDescriptor propertyDescriptor : outputProperties) {
            if (!propertyDescriptor.getName().equals("errorMessages")) {
                assertThat(propertyDescriptor.isSessionVariable(), is(true));
            }
        }
    }

    @Test
    public void testInvalidInput() {
        List<PropertyDescriptor> inputProperties =
                PropertyDescriptor.getInputProperties(TestInvalidSessionPropertyAction.class);
        assertThat("Invalid number of input properties", inputProperties.size(), is(5));
        for (PropertyDescriptor propertyDescriptor : inputProperties) {
            assertThat(propertyDescriptor.isSessionVariable(), is(true));
            assertThat(propertyDescriptor.isRequired(), is(false));
            assertThat(propertyDescriptor.getValidators().size(), is(0));
            assertThat(propertyDescriptor.getCookieAnnotation(), nullValue());
        }
    }

    @Test
    public void testInvalidOutput() {
        List<PropertyDescriptor> outputProperties =
                PropertyDescriptor.getOutputProperties(TestInvalidSessionPropertyAction.class);
        assertThat("Invalid number of output properties", outputProperties.size(), is(6));
        for (PropertyDescriptor propertyDescriptor : outputProperties) {
            if (!propertyDescriptor.getName().equals("errorMessages")) {
                assertThat(propertyDescriptor.isSessionVariable(), is(true));
                assertThat(propertyDescriptor.isRequired(), is(false));
                assertThat(propertyDescriptor.getValidators().size(), is(0));
                assertThat(propertyDescriptor.getCookieAnnotation(), nullValue());
            }
        }
    }
}
