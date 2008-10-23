
package cn.edu.zju.acm.mvc.control;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    PropertyDescriptorInputTest.class,
    PropertyDescriptorOutputTest.class,
    PropertyDescriptorUnsupportedPropertyInputTest.class,
    PropertyDescriptorIntValidatorTest.class,
    PropertyDescriptorFloatValidatorTest.class,
    PropertyDescriptorStringValidatorTest.class,
    PropertyDescriptorDateValidatorTest.class,
    PropertyDescriptorRequiredValidatorTest.class,
    PropertyDescriptorValidatorInvalidTest.class,
    PropertyDescriptorSessionTest.class,
    PropertyDescriptorCookieTest.class,
})
public class PropertyDescriptorTestSuite {

}
