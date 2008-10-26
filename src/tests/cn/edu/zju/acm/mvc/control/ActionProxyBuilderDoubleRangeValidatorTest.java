
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.TestDoubleRangeValidatorAction;

public class ActionProxyBuilderDoubleRangeValidatorTest extends ActionProxyBuilderTestBase {

    @BeforeClass
    public static void init() throws Exception {
        ActionProxyBuilderTestBase.init(TestDoubleRangeValidatorAction.class);
    }

    @Test
    public void testNoneOnDouble() throws Exception {
        req.getParameterMap().put("noneOnDoubleProp", new String[] {"123"});
        proxy.execute(req, resp);
        assertThat(((TestDoubleRangeValidatorAction) proxy).getNoneOnDoubleProp(), is(123.0));
    }

    public void testMinOnDouble() throws Exception {
        req.getParameterMap().put("minOnDoubleProp", new String[] {"1"});
        proxy.execute(req, resp);
        assertThat(((TestDoubleRangeValidatorAction) proxy).getMinOnDoubleProp(), is(1.0));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnDoubleFailure() throws Exception {
        req.getParameterMap().put("minOnDoubleProp", new String[] {"0"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMaxOnDouble() throws Exception {
        req.getParameterMap().put("maxOnDoubleProp", new String[] {"2"});
        proxy.execute(req, resp);
        assertThat(((TestDoubleRangeValidatorAction) proxy).getMaxOnDoubleProp(), is(2.0));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnDoubleFailure() throws Exception {
        req.getParameterMap().put("maxOnDoubleProp", new String[] {"3"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMinAndMaxOnDouble() throws Exception {
        req.getParameterMap().put("minAndMaxOnDoubleProp", new String[] {"1"});
        proxy.execute(req, resp);
        assertThat(((TestDoubleRangeValidatorAction) proxy).getMinAndMaxOnDoubleProp(), is(1.0));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnDoubleMinFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnDoubleProp", new String[] {"0"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnDoubleMaxFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnDoubleProp", new String[] {"3"});
        proxy.execute(req, resp);
    }

    @Test
    public void testNoneOnDoubleArray() throws Exception {
        req.getParameterMap().put("noneOnDoubleArrayProp", new String[] {"0", "1", "2", "3"});
        proxy.execute(req, resp);
        assertThat(((TestDoubleRangeValidatorAction) proxy).getNoneOnDoubleArrayProp(), is(new double[] {0, 1, 2, 3}));
    }

    @Test
    public void testMinOnDoubleArray() throws Exception {
        req.getParameterMap().put("minOnDoubleArrayProp", new String[] {"1", "2", "3"});
        proxy.execute(req, resp);
        assertThat(((TestDoubleRangeValidatorAction) proxy).getMinOnDoubleArrayProp(), is(new double[] {1, 2, 3}));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnDoubleArrayFailure1() throws Exception {
        req.getParameterMap().put("minOnDoubleArrayProp", new String[] {"0", "1", "2", "3"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnDoubleArrayFailure2() throws Exception {
        req.getParameterMap().put("minOnDoubleArrayProp", new String[] {"1", "0", "2", "3"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnDoubleArrayFailure3() throws Exception {
        req.getParameterMap().put("minOnDoubleArrayProp", new String[] {"1", "2", "3", "0"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMaxOnDoubleArray() throws Exception {
        req.getParameterMap().put("maxOnDoubleArrayProp", new String[] {"0", "1", "2"});
        proxy.execute(req, resp);
        assertThat(((TestDoubleRangeValidatorAction) proxy).getMaxOnDoubleArrayProp(), is(new double[] {0, 1, 2}));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnDoubleArrayFailure1() throws Exception {
        req.getParameterMap().put("maxOnDoubleArrayProp", new String[] {"0", "1", "2", "3"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnDoubleArrayFailure2() throws Exception {
        req.getParameterMap().put("maxOnDoubleArrayProp", new String[] {"0", "1", "3", "2"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnDoubleArrayFailure3() throws Exception {
        req.getParameterMap().put("maxOnDoubleArrayProp", new String[] {"3", "0", "1", "2"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMinAndMaxOnDoubleArray() throws Exception {
        req.getParameterMap().put("minAndMaxOnDoubleArrayProp", new String[] {"1", "2"});
        proxy.execute(req, resp);
        assertThat(((TestDoubleRangeValidatorAction) proxy).getMinAndMaxOnDoubleArrayProp(), is(new double[] {1, 2}));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnDoubleArrayMinFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnDoubleArrayProp", new String[] {"1", "0", "2"});
        proxy.execute(req, resp);
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnDoubleArrayMaxFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnDoubleArrayProp", new String[] {"1", "3", "2"});
        proxy.execute(req, resp);
    }
}
