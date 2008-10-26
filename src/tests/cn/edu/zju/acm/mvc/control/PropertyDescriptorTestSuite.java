
package cn.edu.zju.acm.mvc.control;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    PropertyDescriptorInputTest.class,
    PropertyDescriptorOutputTest.class,
    PropertyDescriptorUnsupportedPropertyInputTest.class,
    PropertyDescriptorIntRangeValidatorTest.class,
    PropertyDescriptorDoubleRangeValidatorTest.class,
    PropertyDescriptorStringLengthValidatorTest.class,
    PropertyDescriptorStringPatternValidatorTest.class,
    PropertyDescriptorRequiredValidatorTest.class,
    PropertyDescriptorValidatorInvalidTest.class,
    PropertyDescriptorSessionTest.class,
    PropertyDescriptorSessionInvalidAnnotationsTest.class,
    PropertyDescriptorCookieTest.class,
    PropertyDescriptorConversionErrorTest.class,
})
public class PropertyDescriptorTestSuite {

}
