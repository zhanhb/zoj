
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

public class PropertyDescriptorOutputTest extends PropertyDescriptorTest {

    @BeforeClass
    public static void init() {
        init(TestOutputPropertyAction.class);
        assertThat("Invalid number of output properties", outputPropertyList.size(), is(6));
    }

    protected void check() {
        this.check(this.getPropertyDescriptor(outputPropertyList));
    }

    @Test
    public void testObject() {
        this.type = Object.class;
        this.name = "objProp";
        this.check();
    }

    @Test
    public void testObjectArray() {
        this.type = Object[].class;
        this.name = "objArrayProp";
        this.check();
    }

    @Test
    public void testList() throws Exception {
        this.type = TestOutputPropertyAction.class.getDeclaredMethod("getListProp").getGenericReturnType();
        this.name = "listProp";
        this.check();
    }

    @Test
    public void testMap() throws Exception {
        this.type = TestOutputPropertyAction.class.getDeclaredMethod("getMapProp").getGenericReturnType();
        this.name = "mapProp";
        this.check();
    }

    @Test
    public void testBoolean() throws Exception {
        this.type = boolean.class;
        this.name = "booleanProp";
        this.check();
    }

    @Test
    public void testFieldErrors() throws Exception {
        this.type = Action.class.getDeclaredMethod("getFieldErrors").getGenericReturnType();
        this.name = "fieldErrors";
        this.check();
    }
}
