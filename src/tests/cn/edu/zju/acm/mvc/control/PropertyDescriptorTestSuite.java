
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
    PropertyDescriptorStringLengthValidatorTest.class,
    PropertyDescriptorRequiredValidatorTest.class,
    PropertyDescriptorValidatorInvalidTest.class,
    PropertyDescriptorSessionTest.class,
    PropertyDescriptorCookieTest.class,
    PropertyDescriptorConversionErrorTest.class,
})
public class PropertyDescriptorTestSuite {

}
