
package cn.edu.zju.acm.mvc.control;

import org.junit.Before;

public class ActionExecutorTestBase {

    protected MockHttpServletRequest req;

    protected MockHttpServletResponse resp;

    @Before
    public void setUp() throws Exception {
        this.req = new MockHttpServletRequest();
        this.resp = new MockHttpServletResponse();
    }

    public ActionExecutor build(Class<? extends Action> clazz) throws InstantiationException, IllegalAccessException {
        return new ActionExecutorBuilder().build(new ActionDescriptor(clazz), true).newInstance();
    }
}
