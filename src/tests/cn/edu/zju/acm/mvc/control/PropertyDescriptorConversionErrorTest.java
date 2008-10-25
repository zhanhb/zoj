
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.TestConversionErrorOnPropertyAction;

public class PropertyDescriptorConversionErrorTest extends PropertyDescriptorTest {

    @Test
    public void testInput() {
        List<PropertyDescriptor> inputProperties =
                PropertyDescriptor
                                  .getInputProperties(new MockActionDescriptor(TestConversionErrorOnPropertyAction.class));
        for (PropertyDescriptor propertyDescriptor : inputProperties) {
            if (propertyDescriptor.getType().equals(int.class)) {
                assertThat(propertyDescriptor.getConversionErrorMessageKey(), is("error"));
            } else {
                assertThat(propertyDescriptor.getConversionErrorMessageKey(), nullValue());
            }
        }
    }

    @Test
    public void testOutput() {
        List<PropertyDescriptor> outputProperties =
                PropertyDescriptor
                                  .getOutputProperties(new MockActionDescriptor(TestConversionErrorOnPropertyAction.class));
        for (PropertyDescriptor propertyDescriptor : outputProperties) {
            assertThat(propertyDescriptor.getConversionErrorMessageKey(), nullValue());
        }
    }

    @Test
    public void testInputWithMessageOnAction() {
        MockActionDescriptor actionDescriptor = new MockActionDescriptor(TestConversionErrorOnPropertyAction.class);
        actionDescriptor.setConversionErrorMessageKey("test");
        List<PropertyDescriptor> inputProperties = PropertyDescriptor.getInputProperties(actionDescriptor);
        for (PropertyDescriptor propertyDescriptor : inputProperties) {
            if (propertyDescriptor.getType().equals(int.class)) {
                assertThat(propertyDescriptor.getConversionErrorMessageKey(), is("error"));
            } else if (propertyDescriptor.getType().equals(double.class)) {
                assertThat(propertyDescriptor.getConversionErrorMessageKey(), is("test"));
            } else {
                assertThat(propertyDescriptor.getConversionErrorMessageKey(), nullValue());
            }
        }
    }

    @Test
    public void testOutputWithMessageOnAction() {
        MockActionDescriptor actionDescriptor = new MockActionDescriptor(TestConversionErrorOnPropertyAction.class);
        actionDescriptor.setConversionErrorMessageKey("test");
        List<PropertyDescriptor> outputProperties = PropertyDescriptor.getOutputProperties(actionDescriptor);
        for (PropertyDescriptor propertyDescriptor : outputProperties) {
            assertThat(propertyDescriptor.getConversionErrorMessageKey(), nullValue());
        }
    }
}
