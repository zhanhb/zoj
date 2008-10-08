
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import cn.edu.zju.acm.mvc.control.action.UnsupportedInputPropertyAction;

public class PropertyDescriptorUnsupportedPropertyInputTest {

    @Test
    public void test() {
        assertThat("Invalid number of properties",
                   PropertyDescriptor.getInputProperties(UnsupportedInputPropertyAction.class).size(), is(1));
    }
}
