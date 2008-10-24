
package cn.edu.zju.acm.mvc.control;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import cn.edu.zju.acm.mvc.control.annotation.OneException;
import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;

public class ActionDescriptorTest {

    @Test
    public void testSingleResult() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestSingleResultAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<String, Result>  resultMap = actionDescriptor.getResultMap();
        assertThat(resultMap, notNullValue());
        assertThat(resultMap.size(), is(1));
        
        Result result = resultMap.get("jsp");
        assertThat(result, notNullValue());
        assertThat(result.name(), is("jsp"));
        assertThat(result.value(), is("test.jsp"));
        assertThat(result.type(), is(ResultType.Jsp));
    }
    
    @Test
    public void testResults() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestResultsAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<String, Result>  resultMap = actionDescriptor.getResultMap();
        assertThat(resultMap, notNullValue());
        assertThat(resultMap.size(), is(3));
        
        Iterator<Result> iterator = resultMap.values().iterator();
        Result result = iterator.next();
        assertThat(result, notNullValue());
        assertThat(result, notNullValue());
        assertThat(result.name(), is("jsp"));
        assertThat(result.value(), is("test.jsp"));
        assertThat(result.type(), is(ResultType.Jsp));
        
        result = iterator.next();
        assertThat(result, notNullValue());
        assertThat(result.name(), is("raw"));
        assertThat(result.value(), is("out"));
        assertThat(result.type(), is(ResultType.Raw));
        
        result = iterator.next();
        assertThat(result.name(), is("redirect"));
        assertThat(result.value(), is("/test"));
        assertThat(result.type(), is(ResultType.Redirect));
    }
    
    @Test
    public void testDerivedResults() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestDerivedResultsAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<String, Result>  resultMap = actionDescriptor.getResultMap();
        assertThat(resultMap, notNullValue());
        assertThat(resultMap.size(), is(3));
        
        Iterator<Result> iterator = resultMap.values().iterator();
        Result result = iterator.next();
        assertThat(result, notNullValue());
        assertThat(result.name(), is("jsp"));
        assertThat(result.value(), is("derived.jsp"));
        assertThat(result.type(), is(ResultType.Jsp));
        
        result = iterator.next();
        assertThat(result, notNullValue());
        assertThat(result.name(), is("raw"));
        assertThat(result.value(), is("out"));
        assertThat(result.type(), is(ResultType.Raw));
        
        result = iterator.next();
        assertThat(result, notNullValue());
        assertThat(result.name(), is("redirect"));
        assertThat(result.value(), is("/test"));
        assertThat(result.type(), is(ResultType.Redirect));
    }
    
    @Test
    public void testDuplicateResult() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestDuplicateResultsAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<String, Result>  resultMap = actionDescriptor.getResultMap();
        assertThat(resultMap, notNullValue());
        assertThat(resultMap.size(), is(1));
        
        Result result = resultMap.get("test");
        assertThat(result, notNullValue());
        assertThat(result.name(), is("test"));
        assertThat(result.value(), is("test.jsp"));
        assertThat(result.type(), is(ResultType.Jsp));
    }
    
    @Test
    public void testNoResult() {
        assertThat(ActionDescriptor.getActionDescriptor(TestActionBase.class), nullValue());
    }
    
    @Test
    public void testRawResultNoProperty() {
        assertThat(ActionDescriptor.getActionDescriptor(TestRawResultNoPropertyAction.class), nullValue());
    }
    
    @Test
    public void testRawResultInvalidPropertyType() {
        assertThat(ActionDescriptor.getActionDescriptor(TestRawResultInvalidPropertyTypeAction.class), nullValue());
    }
    
    @Test
    public void testSingleException() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestSingleExceptionAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<Class<? extends Throwable>, OneException>  exceptionMap = actionDescriptor.getExceptionMap();
        assertThat(exceptionMap, notNullValue());
        assertThat(exceptionMap.size(), is(1));
        
        OneException exception = exceptionMap.get(Exception.class);
        assertThat(exception, notNullValue());
        assertThat(exception.exception(), is(new IsEqual<Class<?>>(Exception.class)));
        assertThat(exception.result(), is("jsp"));
    }
    
    @Test
    public void testExceptions() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestExceptionsAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<Class<? extends Throwable>, OneException>  exceptionMap = actionDescriptor.getExceptionMap();
        assertThat(exceptionMap, notNullValue());
        assertThat(exceptionMap.size(), is(2));
        
        Iterator<OneException> iterator = exceptionMap.values().iterator();
        OneException exception = iterator.next();
        assertThat(exception, notNullValue());
        assertThat(exception.exception(), is(new IsEqual<Class<?>>(IllegalArgumentException.class)));
        assertThat(exception.result(), is("jsp"));
        
        exception = iterator.next();
        assertThat(exception, notNullValue());
        assertThat(exception.exception(), is(new IsEqual<Class<?>>(NullPointerException.class)));
        assertThat(exception.result(), is("jsp"));
    }
    
    @Test
    public void testExceptionsOverridden() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestExceptionsOverriddenAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<Class<? extends Throwable>, OneException>  exceptionMap = actionDescriptor.getExceptionMap();
        assertThat(exceptionMap, notNullValue());
        assertThat(exceptionMap.size(), is(1));
        
        OneException exception = exceptionMap.get(Exception.class);
        assertThat(exception, notNullValue());
        assertThat(exception.exception(), is(new IsEqual<Class<?>>(Exception.class)));
        assertThat(exception.result(), is("jsp"));
    }
    
    @Test
    public void testDerivedExceptions() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestDerivedExceptionsAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<Class<? extends Throwable>, OneException>  exceptionMap = actionDescriptor.getExceptionMap();
        assertThat(exceptionMap, notNullValue());
        assertThat(exceptionMap.size(), is(2));
        
        Iterator<OneException> iterator = exceptionMap.values().iterator();
        OneException exception = iterator.next();
        assertThat(exception, notNullValue());
        assertThat(exception.exception(), is(new IsEqual<Class<?>>(IllegalArgumentException.class)));
        assertThat(exception.result(), is("test"));
        
        exception = iterator.next();
        assertThat(exception, notNullValue());
        assertThat(exception.exception(), is(new IsEqual<Class<?>>(NullPointerException.class)));
        assertThat(exception.result(), is("jsp"));        
    }
    
    @Test
    public void testDerivedExceptionsOverridden() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestDerivedExceptionsOverriddenAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<Class<? extends Throwable>, OneException>  exceptionMap = actionDescriptor.getExceptionMap();
        assertThat(exceptionMap, notNullValue());
        assertThat(exceptionMap.size(), is(1));
        
        OneException exception = exceptionMap.get(Exception.class);
        assertThat(exception, notNullValue());
        assertThat(exception.exception(), is(new IsEqual<Class<?>>(Exception.class)));
        assertThat(exception.result(), is("test"));
    }
    
    @Test
    public void testExceptionInvalidResult() {
        ActionDescriptor actionDescriptor = ActionDescriptor.getActionDescriptor(TestExceptionInvalidResultAction.class);
        assertThat(actionDescriptor, notNullValue());
        
        Map<Class<? extends Throwable>, OneException>  exceptionMap = actionDescriptor.getExceptionMap();
        assertThat(exceptionMap, notNullValue());
        assertThat(exceptionMap.size(), is(0));
    }
}
