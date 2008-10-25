
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PropertyDescriptorUnsupportedPropertyInputTest {

    @Test
    public void test() {
        assertThat(
                   "Invalid number of properties",
                   PropertyDescriptor
                                     .getInputProperties(
                                                         new MockActionDescriptor(
                                                                                  TestUnsupportedInputPropertyAction.class))
                                     .size(), is(0));
    }
}
