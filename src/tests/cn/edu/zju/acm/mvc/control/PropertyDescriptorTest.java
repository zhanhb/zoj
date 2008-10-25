
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyDescriptorTest {

    protected ActionDescriptor owner;

    protected String name;

    protected Type type;


    public void setUp() {
        this.owner = null;
        this.name = null;
        this.type = null;
    }

    protected PropertyDescriptor getPropertyDescriptor(List<PropertyDescriptor> propertyDescriptorList) {
        Map<String, PropertyDescriptor> propertyDescriptorMap = new HashMap<String, PropertyDescriptor>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptorList) {
            propertyDescriptorMap.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        PropertyDescriptor propertyDescriptor = propertyDescriptorMap.get(this.name);
        assertThat(String.format("Property '%s' is not available", this.name), propertyDescriptor, notNullValue());
        return propertyDescriptor;
    }
}
