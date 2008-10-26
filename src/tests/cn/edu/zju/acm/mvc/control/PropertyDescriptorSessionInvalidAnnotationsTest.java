
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.TestSessionInvalidAnnotationsPropertyAction;
import cn.edu.zju.acm.mvc.control.annotation.TestSessionPropertyAction;

public class PropertyDescriptorSessionInvalidAnnotationsTest extends PropertyDescriptorTest {

    @BeforeClass
    public static void init() {
        init(TestSessionInvalidAnnotationsPropertyAction.class);
        assertThat("Invalid number of input properties", inputPropertyList.size(), is(5));
        assertThat("Invalid number of output properties", outputPropertyList.size(), is(6));
    }

    @Test
    public void testInputObject() {
        this.type = Object.class;
        this.name = "objProp";
        this.sessionVariable = true;
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }

    @Test
    public void testOutputObject() {
        this.type = Object.class;
        this.name = "objProp";
        this.sessionVariable = true;
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }

    @Test
    public void testInputFileArray() {
        this.type = File[].class;
        this.name = "fileArrayProp";
        this.sessionVariable = true;
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }

    @Test
    public void testOutputFileArray() {
        this.type = File[].class;
        this.name = "fileArrayProp";
        this.sessionVariable = true;
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }

    @Test
    public void testInputFileSet() throws Exception {
        this.type = TestSessionPropertyAction.class.getDeclaredMethod("getFileSetProp").getGenericReturnType();
        this.name = "fileSetProp";
        this.sessionVariable = true;
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }

    @Test
    public void testOutputFileSet() throws Exception {
        this.type = TestSessionPropertyAction.class.getDeclaredMethod("getFileSetProp").getGenericReturnType();
        this.name = "fileSetProp";
        this.sessionVariable = true;
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }

    @Test
    public void testInputMap() throws Exception {
        this.type = TestSessionPropertyAction.class.getDeclaredMethod("getMapProp").getGenericReturnType();
        this.name = "mapProp";
        this.sessionVariable = true;
        this.check(this.getPropertyDescriptor(inputPropertyList));
    }

    @Test
    public void testOutputMap() throws Exception {
        this.type = TestSessionPropertyAction.class.getDeclaredMethod("getMapProp").getGenericReturnType();
        this.name = "mapProp";
        this.sessionVariable = true;
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }
    /*
     * @Test public void testInvalidInput() { List<PropertyDescriptor> inputProperties =
     * PropertyDescriptor.getInputProperties(new MockActionDescriptor(TestInvalidSessionPropertyAction.class));
     * assertThat("Invalid number of input properties", inputProperties.size(), is(5)); for (PropertyDescriptor
     * propertyDescriptor : inputProperties) { assertThat(propertyDescriptor.isSessionVariable(), is(true));
     * assertThat(propertyDescriptor.getRequiredAnnotation(), nullValue());
     * assertThat(propertyDescriptor.getValidators().size(), is(0));
     * assertThat(propertyDescriptor.getCookieAnnotation(), nullValue()); } }
     * 
     * @Test public void testInvalidOutput() { List<PropertyDescriptor> outputProperties = PropertyDescriptor
     * .getOutputProperties(new MockActionDescriptor(TestInvalidSessionPropertyAction.class));
     * assertThat("Invalid number of output properties", outputProperties.size(), is(6)); for (PropertyDescriptor
     * propertyDescriptor : outputProperties) { if (!propertyDescriptor.getName().equals("fieldErrors")) {
     * assertThat(propertyDescriptor.isSessionVariable(), is(true));
     * assertThat(propertyDescriptor.getRequiredAnnotation(), nullValue());
     * assertThat(propertyDescriptor.getValidators().size(), is(0));
     * assertThat(propertyDescriptor.getCookieAnnotation(), nullValue()); } } }
     */
}
