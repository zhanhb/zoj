
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.TestConversionErrorOnPropertyAction;

public class PropertyDescriptorConversionErrorTest extends PropertyDescriptorTest {

    @BeforeClass
    public static void init() {
        init(TestConversionErrorOnPropertyAction.class);
        assertThat("Invalid number of input properties", inputPropertyList.size(), is(3));
        assertThat("Invalid number of output properties", outputPropertyList.size(), is(4));
    }
    
    @After
    public void tearDown() {
        actionDescriptor.setConversionErrorMessageKey(null);
    }

    @Test
    public void testInputInt() {
        this.type = this.rawType = int.class;
        this.name = "intProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.conversionErrorMessageKey = "error";
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }
    
    @Test
    public void testInputDouble() {
        this.type = this.rawType = double.class;
        this.name = "doubleProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }
    
    @Test
    public void testInputString() {
        this.type = this.rawType = String.class;
        this.name = "stringProp";
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }
    
    @Test
    public void testOutputInt() {
        this.type = int.class;
        this.name = "intProp";
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
    
    @Test
    public void testOutputDouble() {
        this.type = double.class;
        this.name = "doubleProp";
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
    
    @Test
    public void testOutputString() {
        this.type = String.class;
        this.name = "stringProp";
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
    
    @Test
    public void testInputIntWithActionMessage() {
        actionDescriptor.setConversionErrorMessageKey("test");
        this.type = this.rawType = int.class;
        this.name = "intProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.conversionErrorMessageKey = "error";
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }
    
    @Test
    public void testInputDoubleWithActionMessage() {
        actionDescriptor.setConversionErrorMessageKey("test");
        this.type = this.rawType = double.class;
        this.name = "doubleProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.conversionErrorMessageKey = "test";
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }
    
    @Test
    public void testInputStringWithActionMessage() {
        actionDescriptor.setConversionErrorMessageKey("test");
        this.type = this.rawType = String.class;
        this.name = "stringProp";
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }
    
    @Test
    public void testOutputIntWithActionMessage() {
        actionDescriptor.setConversionErrorMessageKey("test");
        this.type = int.class;
        this.name = "intProp";
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
    
    @Test
    public void testOutputDoubleWithActionMessage() {
        actionDescriptor.setConversionErrorMessageKey("test");
        this.type = double.class;
        this.name = "doubleProp";
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
    
    @Test
    public void testOutputStringWithActionMessage() {
        actionDescriptor.setConversionErrorMessageKey("test");
        this.type = String.class;
        this.name = "stringProp";
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
}
