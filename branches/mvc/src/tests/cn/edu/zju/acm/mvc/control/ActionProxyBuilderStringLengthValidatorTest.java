
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.TestStringLengthValidatorAction;

public class ActionProxyBuilderStringLengthValidatorTest extends ActionProxyBuilderTestBase {

    @BeforeClass
    public static void init() throws Exception {
        ActionProxyBuilderTestBase.init(TestStringLengthValidatorAction.class);
    }

    @Test
    public void testNoneOnString() throws Exception {
        req.getParameterMap().put("noneOnStringProp", new String[] {"123"});
        proxy.execute(req, resp);
        assertThat(((TestStringLengthValidatorAction) proxy).getNoneOnStringProp(), is("123"));
    }

    public void testMinOnString() throws Exception {
        req.getParameterMap().put("minOnStringProp", new String[] {"1"});
        proxy.execute(req, resp);
        assertThat(((TestStringLengthValidatorAction) proxy).getMinOnStringProp(), is("1"));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnStringFailure() throws Exception {
        req.getParameterMap().put("minOnStringProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test
    public void testMaxOnString() throws Exception {
        req.getParameterMap().put("maxOnStringProp", new String[] {"12"});
        proxy.execute(req, resp);
        assertThat(((TestStringLengthValidatorAction) proxy).getMaxOnStringProp(), is("12"));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnStringFailure() throws Exception {
        req.getParameterMap().put("maxOnStringProp", new String[] {"123"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMinAndMaxOnString() throws Exception {
        req.getParameterMap().put("minAndMaxOnStringProp", new String[] {"1"});
        proxy.execute(req, resp);
        assertThat(((TestStringLengthValidatorAction) proxy).getMinAndMaxOnStringProp(), is("1"));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnStringMinFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnStringProp", new String[] {""});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnStringMaxFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnStringProp", new String[] {"123"});
        proxy.execute(req, resp);
    }

    @Test
    public void testNoneOnStringArray() throws Exception {
        req.getParameterMap().put("noneOnStringArrayProp", new String[] {"", "1", "12", "123"});
        proxy.execute(req, resp);
        assertThat(((TestStringLengthValidatorAction) proxy).getNoneOnStringArrayProp(), is(new String[] {"", "1", "12", "123"}));
    }

    @Test
    public void testMinOnStringArray() throws Exception {
        req.getParameterMap().put("minOnStringArrayProp", new String[] {"1", "12", "123"});
        proxy.execute(req, resp);
        assertThat(((TestStringLengthValidatorAction) proxy).getMinOnStringArrayProp(), is(new String[] {"1", "12", "123"}));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnStringArrayFailure1() throws Exception {
        req.getParameterMap().put("minOnStringArrayProp", new String[] {"", "1", "12", "123"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnStringArrayFailure2() throws Exception {
        req.getParameterMap().put("minOnStringArrayProp", new String[] {"1", "", "12", "123"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMinOnStringArrayFailure3() throws Exception {
        req.getParameterMap().put("minOnStringArrayProp", new String[] {"1", "12", "123", ""});
        proxy.execute(req, resp);
    }

    @Test
    public void testMaxOnStringArray() throws Exception {
        req.getParameterMap().put("maxOnStringArrayProp", new String[] {"", "1", "12"});
        proxy.execute(req, resp);
        assertThat(((TestStringLengthValidatorAction) proxy).getMaxOnStringArrayProp(), is(new String[] {"", "1", "12"}));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnStringArrayFailure1() throws Exception {
        req.getParameterMap().put("maxOnStringArrayProp", new String[] {"", "1", "12", "123"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnStringArrayFailure2() throws Exception {
        req.getParameterMap().put("maxOnStringArrayProp", new String[] {"", "1", "123", "12"});
        proxy.execute(req, resp);
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testMaxOnStringArrayFailure3() throws Exception {
        req.getParameterMap().put("maxOnStringArrayProp", new String[] {"123", "", "1", "12"});
        proxy.execute(req, resp);
    }

    @Test
    public void testMinAndMaxOnStringArray() throws Exception {
        req.getParameterMap().put("minAndMaxOnStringArrayProp", new String[] {"1", "12"});
        proxy.execute(req, resp);
        assertThat(((TestStringLengthValidatorAction) proxy).getMinAndMaxOnStringArrayProp(), is(new String[] {"1", "12"}));
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnStringArrayMinFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnStringArrayProp", new String[] {"1", "", "12"});
        proxy.execute(req, resp);
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testMinAndMaxOnStringArrayMaxFailure() throws Exception {
        req.getParameterMap().put("minAndMaxOnStringArrayProp", new String[] {"1", "123", "2"});
        proxy.execute(req, resp);
    }
}
