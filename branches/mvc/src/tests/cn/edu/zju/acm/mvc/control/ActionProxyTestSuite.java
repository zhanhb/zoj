package cn.edu.zju.acm.mvc.control;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    ActionProxyBuilderInitializationTest.class,
    ActionProxyBuilderIntRangeValidatorTest.class,
    ActionProxyBuilderDoubleRangeValidatorTest.class,
    ActionProxyBuilderStringLengthValidatorTest.class,
    ActionProxyBuilderStringPatternValidatorTest.class,
})
public class ActionProxyTestSuite {
}
