
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.TestCookiePropertyAction;

public class PropertyDescriptorCookieTest extends PropertyDescriptorTest {

    @BeforeClass
    public static void init() {
        init(TestCookiePropertyAction.class);
        assertThat("Invalid number of input properties", inputPropertyList.size(), is(2));
        assertThat("Invalid number of output properties", outputPropertyList.size(), is(4));
    }

    @Test
    public void testInputInt() {
        this.type = this.rawType = int.class;
        this.name = "intProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.cookie = true;
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }

    @Test
    public void testOutputInt() {
        this.type = int.class;
        this.name = "intProp";
        this.cookie = true;
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
    
    @Test
    public void testInputString() {
        this.type = this.rawType = String.class;
        this.name = "stringProp";
        this.cookie = true;
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }
    
    @Test
    public void testOutputString() {
        this.type = String.class;
        this.name = "stringProp";
        this.cookie = true;
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }

    @Test
    public void testOutputObject() {
        this.type = Object.class;
        this.name = "objProp";
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
}
