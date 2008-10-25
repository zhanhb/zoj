
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.TestCookiePropertyAction;

public class PropertyDescriptorCookieTest extends PropertyDescriptorTest {

    @Test
    public void testInput() {
        List<PropertyDescriptor> inputProperties =
                PropertyDescriptor.getInputProperties(new MockActionDescriptor(TestCookiePropertyAction.class));
        assertThat("Invalid number of input properties", inputProperties.size(), is(1));
        for (PropertyDescriptor propertyDescriptor : inputProperties) {
            assertThat(propertyDescriptor.getCookieAnnotation(), notNullValue());
        }
    }

    @Test
    public void testOutput() {
        List<PropertyDescriptor> outputProperties =
                PropertyDescriptor.getOutputProperties(new MockActionDescriptor(TestCookiePropertyAction.class));
        assertThat("Invalid number of output properties", outputProperties.size(), is(3));
        for (PropertyDescriptor propertyDescriptor : outputProperties) {
            if (propertyDescriptor.getName().startsWith("string")) {
                assertThat(propertyDescriptor.getCookieAnnotation(), notNullValue());
            } else {
                assertThat(propertyDescriptor.getCookieAnnotation(), nullValue());
            }
        }
    }
}
