
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

public class ActionProxyBuilderTestBase {

    protected ActionProxy proxy;

    protected MockHttpServletRequest req;

    protected MockHttpServletResponse resp;

    private static Class<? extends ActionProxy> proxyClass;

    public static void init(Class<? extends Action> actionClass) throws Exception {
        ActionProxyBuilder builder = new ActionProxyBuilder();
        proxyClass = builder.build(ActionDescriptor.getActionDescriptor(actionClass), null, true);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CheckClassAdapter.verify(new ClassReader(builder.getClassContent()), false, pw);
        assertThat(sw.toString(), is(""));

    }

    @Before
    public void setUp() throws Exception {
        this.req = new MockHttpServletRequest();
        this.resp = new MockHttpServletResponse();
        this.proxy = proxyClass.newInstance();
    }
}
