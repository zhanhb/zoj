
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.TestIntRangeValidatorAction;

public class ActionProxyBuilderIntRangeValidatorTest extends ActionProxyBuilderTestBase {

    @BeforeClass
    public static void init() throws Exception {
        ActionProxyBuilderTestBase.init(TestIntRangeValidatorAction.class);
    }

    @Test
    public void testNoneOnInt() throws Exception {
        req.getParameterMap().put("noneOnIntProp", new String[] {"123"});
        proxy.execute(req, resp);
        assertThat(((TestIntRangeValidatorAction) proxy).getNoneOnIntProp(), is(123));
    }

    public void testMinOnInt() throws Exception {
        req.getParameterMap().put("minOnIntProp", new String[] {"1"});
        proxy.execute(req, resp);
        assertThat(((TestIntRangeValidatorAction) proxy).getMinOnIntProp(), is(1));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnIntFailure() throws Exception {
        req.getParameterMap().put("minOnIntProp", new String[] {"0"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMaxOnInt() throws Exception {
        req.getParameterMap().put("maxOnIntProp", new String[] {"2"});
        proxy.execute(req, resp);
        assertThat(((TestIntRangeValidatorAction) proxy).getMaxOnIntProp(), is(2));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnIntFailure() throws Exception {
        req.getParameterMap().put("maxOnIntProp", new String[] {"3"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMinAndMaxOnInt() throws Exception {
        req.getParameterMap().put("minAndMaxOnIntProp", new String[] {"1"});
        proxy.execute(req, resp);
        assertThat(((TestIntRangeValidatorAction) proxy).getMinAndMaxOnIntProp(), is(1));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnIntMinFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnIntProp", new String[] {"0"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnIntMaxFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnIntProp", new String[] {"3"});
        proxy.execute(req, resp);
    }

    @Test
    public void testNoneOnIntArray() throws Exception {
        req.getParameterMap().put("noneOnIntArrayProp", new String[] {"0", "1", "2", "3"});
        proxy.execute(req, resp);
        assertThat(((TestIntRangeValidatorAction) proxy).getNoneOnIntArrayProp(), is(new int[] {0, 1, 2, 3}));
    }

    @Test
    public void testMinOnIntArray() throws Exception {
        req.getParameterMap().put("minOnIntArrayProp", new String[] {"1", "2", "3"});
        proxy.execute(req, resp);
        assertThat(((TestIntRangeValidatorAction) proxy).getMinOnIntArrayProp(), is(new int[] {1, 2, 3}));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnIntArrayFailure1() throws Exception {
        req.getParameterMap().put("minOnIntArrayProp", new String[] {"0", "1", "2", "3"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnIntArrayFailure2() throws Exception {
        req.getParameterMap().put("minOnIntArrayProp", new String[] {"1", "0", "2", "3"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnIntArrayFailure3() throws Exception {
        req.getParameterMap().put("minOnIntArrayProp", new String[] {"1", "2", "3", "0"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMaxOnIntArray() throws Exception {
        req.getParameterMap().put("maxOnIntArrayProp", new String[] {"0", "1", "2"});
        proxy.execute(req, resp);
        assertThat(((TestIntRangeValidatorAction) proxy).getMaxOnIntArrayProp(), is(new int[] {0, 1, 2}));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnIntArrayFailure1() throws Exception {
        req.getParameterMap().put("maxOnIntArrayProp", new String[] {"0", "1", "2", "3"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnIntArrayFailure2() throws Exception {
        req.getParameterMap().put("maxOnIntArrayProp", new String[] {"0", "1", "3", "2"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnIntArrayFailure3() throws Exception {
        req.getParameterMap().put("maxOnIntArrayProp", new String[] {"3", "0", "1", "2"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMinAndMaxOnIntArray() throws Exception {
        req.getParameterMap().put("minAndMaxOnIntArrayProp", new String[] {"1", "2"});
        proxy.execute(req, resp);
        assertThat(((TestIntRangeValidatorAction) proxy).getMinAndMaxOnIntArrayProp(), is(new int[] {1, 2}));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnIntArrayMinFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnIntArrayProp", new String[] {"1", "0", "2"});
        proxy.execute(req, resp);
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnIntArrayMaxFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnIntArrayProp", new String[] {"1", "3", "2"});
        proxy.execute(req, resp);
    }
}
