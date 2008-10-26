
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.validator.TestStringPatternValidatorAction;

public class ActionProxyBuilderStringPatternValidatorTest extends ActionProxyBuilderTestBase {

    @BeforeClass
    public static void init() throws Exception {
        ActionProxyBuilderTestBase.init(TestStringPatternValidatorAction.class);
    }

    @Test
    public void testString() throws Exception {
        req.getParameterMap().put("stringProp", new String[] {"aa"});
        proxy.execute(req, resp);
        assertThat(((TestStringPatternValidatorAction) proxy).getStringProp(), is("aa"));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testStringFailure() throws Exception {
        req.getParameterMap().put("stringProp", new String[] {"ab"});
        proxy.execute(req, resp);
    }

    @Test
    public void testStringArray() throws Exception {
        req.getParameterMap().put("stringArrayProp", new String[] {"", "a", "aa", "aaa"});
        proxy.execute(req, resp);
        assertThat(((TestStringPatternValidatorAction) proxy).getStringArrayProp(), is(new String[] {"", "a", "aa", "aaa"}));
    }

    @Test(expected = FieldInitializationErrorException.class)
    public void testStringArrayFailure1() throws Exception {
        req.getParameterMap().put("stringArrayProp", new String[] {"b", "a", "aa", "aaa"});
        proxy.execute(req, resp);
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testStringArrayFailure2() throws Exception {
        req.getParameterMap().put("stringArrayProp", new String[] {"", "ba", "aa", "aaa"});
        proxy.execute(req, resp);
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testStringArrayFailure3() throws Exception {
        req.getParameterMap().put("stringArrayProp", new String[] {"", "a", "a a", "aaa"});
        proxy.execute(req, resp);
    }
    
    @Test(expected = FieldInitializationErrorException.class)
    public void testStringArrayFailure4() throws Exception {
        req.getParameterMap().put("stringArrayProp", new String[] {"", "a", "aa", " "});
        proxy.execute(req, resp);
    }
}
