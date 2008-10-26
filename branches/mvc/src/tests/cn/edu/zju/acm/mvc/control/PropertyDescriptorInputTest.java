
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

public class PropertyDescriptorInputTest extends PropertyDescriptorTest {

    @BeforeClass
    public static void init() {
        init(TestInputPropertyAction.class);
        assertThat("Invalid number of input properties", inputPropertyList.size(), is(12));
    }

    protected void check() {
        super.check(this.getPropertyDescriptor(inputPropertyList));
    }

    @Test
    public void testInt() {
        this.type = this.rawType = int.class;
        this.name = "intProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testLong() {
        this.type = this.rawType = long.class;
        this.name = "longProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testDouble() {
        this.type = this.rawType = double.class;
        this.name = "doubleProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testIntArray() {
        this.type = this.rawType = int[].class;
        this.componentType = int.class;
        this.name = "intArrayProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testLongArray() {
        this.type = this.rawType = long[].class;
        this.componentType = long.class;
        this.name = "longArrayProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testDoubleArray() {
        this.type = this.rawType = double[].class;
        this.componentType = double.class;
        this.name = "doubleArrayProp";
        this.conversionExceptionClasses.add(NumberFormatException.class);
        this.check();
    }

    @Test
    public void testBoolean() {
        this.type = this.rawType = boolean.class;
        this.name = "booleanProp";
        this.check();
    }

    @Test
    public void testString() {
        this.type = this.rawType = String.class;
        this.name = "stringProp";
        this.check();
    }

    @Test
    public void testDate() {
        this.type = this.rawType = Date.class;
        this.name = "dateProp";
        this.conversionExceptionClasses.add(ParseException.class);
        this.check();
    }

    @Test
    public void testFile() {
        this.type = this.rawType = File.class;
        this.name = "fileProp";
        // TODO
        this.check();
    }

    @Test
    public void testStringArray() {
        this.type = this.rawType = String[].class;
        this.componentType = String.class;
        this.name = "stringArrayProp";
        this.check();
    }

    @Test
    public void testDateArray() {
        this.type = this.rawType = Date[].class;
        this.componentType = Date.class;
        this.name = "dateArrayProp";
        this.conversionExceptionClasses.add(ParseException.class);
        this.check();
    }
}
