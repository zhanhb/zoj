
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PropertyDescriptorOutputTest {

    @Test
    public void test() {
        assertThat("Invalid number of properties",
                   PropertyDescriptor.getOutputProperties(new MockActionDescriptor(TestOutputPropertyAction.class))
                                     .size(), is(6));
    }
}
