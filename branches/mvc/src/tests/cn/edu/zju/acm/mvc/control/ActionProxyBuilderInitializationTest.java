
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.text.DateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

public class ActionProxyBuilderInitializationTest extends ActionProxyBuilderTestBase {

    @BeforeClass
    public static void init() throws Exception {
        ActionProxyBuilderTestBase.init(TestPropertyInitializationAction.class);
    }

    @Test
    public void testInt() throws Exception {
        req.getParameterMap().put("intProp", new String[] {"123"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getIntProp(), is(123));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testIntConversionError() throws Exception {
        req.getParameterMap().put("intProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test
    public void testLong() throws Exception {
        req.getParameterMap().put("longProp", new String[] {"12345678901"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getLongProp(), is(12345678901L));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testLongConversionError() throws Exception {
        req.getParameterMap().put("longProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test
    public void testDouble() throws Exception {
        req.getParameterMap().put("doubleProp", new String[] {"1.0"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getDoubleProp(), is(1.0));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testDoubleConversionError() throws Exception {
        req.getParameterMap().put("doubleProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test
    public void testBoolean() throws Exception {
        req.getParameterMap().put("booleanProp", new String[] {""});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).isBooleanProp(), is(true));
    }

    @Test
    public void testString() throws Exception {
        req.getParameterMap().put("stringProp", new String[] {"test"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getStringProp(), is("test"));
    }

    @Test
    public void testDate() throws Exception {
        req.getParameterMap().put("dateProp", new String[] {"2008-01-01"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getDateProp(),
                   is(DateFormat.getDateInstance(DateFormat.SHORT).parse("2008-01-01")));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testDateConversionError() throws Exception {
        req.getParameterMap().put("dateProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test
    public void testFile() throws Exception {
        // TODO
    }

    @Test
    public void testIntArray() throws Exception {
        req.getParameterMap().put("intArrayProp", new String[] {"1", "2", "3"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getIntArrayProp(), is(new int[] {1, 2, 3}));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testIntArrayConversionError() throws Exception {
        req.getParameterMap().put("intArrayProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test
    public void testLongArray() throws Exception {
        req.getParameterMap().put("longArrayProp", new String[] {"12345678901", "12345678902", "12345678903"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getLongArrayProp(), is(new long[] {12345678901L,
                                                                                                 12345678902L,
                                                                                                 12345678903L}));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testLongArrayConversionError() throws Exception {
        req.getParameterMap().put("longArrayProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test
    public void testDoubleArray() throws Exception {
        req.getParameterMap().put("doubleArrayProp", new String[] {"1.0", "2.0", "3.0"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getDoubleArrayProp(), is(new double[] {1.0, 2.0, 3.0}));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testDoubleArrayConversionError() throws Exception {
        req.getParameterMap().put("doubleArrayProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test
    public void testStringArray() throws Exception {
        req.getParameterMap().put("stringArrayProp", new String[] {"1", "2", "3"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getStringArrayProp(), is(new String[] {"1", "2", "3"}));
    }

    @Test
    public void testDateArray() throws Exception {
        req.getParameterMap().put("dateArrayProp", new String[] {"2008-01-01", "2008-01-02", "2008-01-03"});
        proxy.execute(req, resp);
        assertThat(((TestPropertyInitializationAction) proxy).getDateArrayProp(),
                   is(new Date[] {DateFormat.getDateInstance(DateFormat.SHORT).parse("2008-01-01"),
                                  DateFormat.getDateInstance(DateFormat.SHORT).parse("2008-01-02"),
                                  DateFormat.getDateInstance(DateFormat.SHORT).parse("2008-01-03")}));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testDateArrayConversionError() throws Exception {
        req.getParameterMap().put("dateArrayProp", new String[] {""});
        proxy.execute(req, resp);
    }
}
