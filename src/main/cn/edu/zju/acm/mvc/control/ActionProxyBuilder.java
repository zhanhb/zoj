
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import cn.edu.zju.acm.mvc.control.annotation.OneException;
import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.onlinejudge.util.Pair;

public class ActionProxyBuilder {

    private static ActionProxyClassLoader actionExecutorClassLoader = new ActionProxyClassLoader();

    private Logger logger = Logger.getLogger(ActionProxyBuilder.class);

    private static final int THIS = 0;

    private static final int REQ = 1;

    private static final int RESP = 2;

    private static final int RESULT = 3;

    private int session = -1;

    private int cookieMap = -1;

    private int variableIndex = 4;

    private boolean debugMode;

    private ClassWriter cw;

    private String parentInternalName;

    private String internalName;

    private Class<?> actionClass;

    private ActionDescriptor actionDescriptor;

    public Class<? extends ActionProxy> build(ActionDescriptor actionDescriptor, boolean debugMode) {
        this.actionDescriptor = actionDescriptor;
        this.actionClass = actionDescriptor.getActionClass();
        this.debugMode = debugMode;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        this.parentInternalName = Type.getInternalName(this.actionClass);
        this.internalName = this.parentInternalName + "Proxy";

        // public class XXXProxy extends XXXX implements ActionProxy
        this.cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, this.internalName, null,
                      this.parentInternalName, new String[] {Type.getInternalName(ActionProxy.class)});
        if (debugMode) {
            // private Logger logger;
            FieldVisitor fv =
                    this.cw.visitField(Opcodes.ACC_PRIVATE, "logger", Type.getDescriptor(Logger.class), null, null);
            fv.visitEnd();
        }
        this.buildConstructor(this.parentInternalName, this.internalName, debugMode);
        this.buildExecute();
        this.cw.visitEnd();
        return ActionProxyBuilder.actionExecutorClassLoader.defineClass(this.actionClass.getName() + "Proxy",
                                                                        this.cw.toByteArray());
    }

    private void buildConstructor(String parentInternalName, String internalName, boolean debugMode) {
        // public XXXProxy() {
        MethodVisitor mv = this.cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();

        // super()
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, parentInternalName, "<init>", "()V");

        if (debugMode) {
            // logger = Logger.getLogger("XXXProxy")
            mv.visitVarInsn(Opcodes.ALOAD, THIS);
            mv.visitLdcInsn(this.actionClass.getName() + "Proxy");
            MethodInvocationUtil.invokeStatic(mv, Logger.class, "getLogger", String.class);
            mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, "logger", Type.getDescriptor(Logger.class));
        }

        mv.visitInsn(Opcodes.RETURN);
        mv.visitEnd();
    }

    private void buildExecute() {
        // public String execute(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        MethodVisitor mv =
                this.cw.visitMethod(Opcodes.ACC_PUBLIC, "execute",
                                    MethodInvocationUtil.getMethodDescriptor(String.class, HttpServletRequest.class,
                                                                             HttpServletResponse.class), null,
                                    new String[] {Type.getInternalName(Exception.class)});
        mv.visitCode();
        if (this.debugMode) {
            loggerDebug(mv, "Enter " + ActionProxyBuilder.this.actionClass.getName());
        }
        this.initializeSession(mv);
        this.initializeCookieMap(mv);

        // result = null
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitVarInsn(Opcodes.ASTORE, RESULT);

        Label startTryCatchLabel = new Label();
        Label endTryCatchLabel = new Label();
        List<Label> labelList = new ArrayList<Label>();
        List<OneException> exceptionList = new ArrayList<OneException>();
        for (OneException exception : this.actionDescriptor.getExceptionMap().values()) {
            exceptionList.add(exception);
            Label label = new Label();
            labelList.add(label);
            mv.visitTryCatchBlock(startTryCatchLabel, labelList.get(0), label,
                                  Type.getInternalName(exception.exception()));
        }
        // start_try_catch:
        mv.visitLabel(startTryCatchLabel);
        
        this.initializeProperties(mv);
        
        // result = super.execute()
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.internalName, "execute",
                           MethodInvocationUtil.getMethodDescriptor(Action.class, "execute"));
        mv.visitVarInsn(Opcodes.ASTORE, RESULT);
        
        for (int i = 0; i < exceptionList.size(); ++i) {
            // goto end_try_catch
            mv.visitJumpInsn(Opcodes.GOTO, endTryCatchLabel);

            // catch(XXXException e)
            mv.visitLabel(labelList.get(i));
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);

            if (this.debugMode) {
                loggerDebug(mv, "Catch exception " + exceptionList.get(i).exception().getName(), this.variableIndex);
            }

            // result = "zzz";
            mv.visitLdcInsn(exceptionList.get(i).result());
            mv.visitVarInsn(Opcodes.ASTORE, RESULT);
        }
        // end_try_catch:
        mv.visitLabel(endTryCatchLabel);

        mv.visitEnd();
    }

    private void initializeCookieMap(MethodVisitor mv) {
        int count = 0;
        for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getInputPropertyMap().values()) {
            if (propertyDescriptor.getCookieAnnotation() != null) {
                count++;
            }
        }
        if (count <= 4) {
            // we don't use cookieMap when there are less than 4 cookie variables.
            return;
        }

        this.cookieMap = this.variableIndex++;
        int cookieArray = this.variableIndex;
        int length = this.variableIndex + 1;
        int i = this.variableIndex + 2;
        int cookie = this.variableIndex + 3;

        // cookieMap = new HashMap();
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(HashMap.class));
        mv.visitInsn(Opcodes.DUP);
        MethodInvocationUtil.invokeConstructor(mv, HashMap.class);
        mv.visitVarInsn(Opcodes.ASTORE, this.cookieMap);

        // cookieArray = req.getCookies();
        mv.visitVarInsn(Opcodes.ALOAD, REQ);
        MethodInvocationUtil.invokeInterface(mv, HttpServletRequest.class, "getCookies");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ASTORE, cookieArray);

        // length = cookieArray.length;
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        mv.visitVarInsn(Opcodes.ISTORE, length);

        // i = 0
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, i);

        Label forTestLabel = new Label();
        // goto for_test
        mv.visitJumpInsn(Opcodes.GOTO, forTestLabel);

        Label forBodyLabel = new Label();
        // for_body
        mv.visitLabel(forBodyLabel);

        // cookie = cookieArray[i]
        mv.visitVarInsn(Opcodes.ALOAD, cookieArray);
        mv.visitVarInsn(Opcodes.ILOAD, i);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitVarInsn(Opcodes.ASTORE, cookie);

        // cookieMap.put(cookie.getName(), cookie);
        mv.visitVarInsn(Opcodes.ALOAD, cookieMap);
        mv.visitVarInsn(Opcodes.ALOAD, cookie);
        MethodInvocationUtil.invokeVirtual(mv, Cookie.class, "getName");
        mv.visitVarInsn(Opcodes.ALOAD, cookie);
        MethodInvocationUtil.invokeInterface(mv, Map.class, "put", Object.class, Object.class);
        mv.visitInsn(Opcodes.POP);

        // i++
        mv.visitIincInsn(i, 1);

        // for_test
        mv.visitLabel(forTestLabel);

        // if (i < length) goto for_body
        mv.visitVarInsn(Opcodes.ILOAD, i);
        mv.visitVarInsn(Opcodes.ILOAD, length);
        mv.visitJumpInsn(Opcodes.IF_ICMPLT, forBodyLabel);
    }

    private void initializeProperties(MethodVisitor mv) {
        for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getInputPropertyMap().values()) {
            if (propertyDescriptor.isSessionVariable()) {
                this.initializeSessionProperty(mv, propertyDescriptor);
            } else {
                this.initializeNonSessionProperty(mv, propertyDescriptor);
            }
        }
    }

    private void logInitialization(MethodVisitor mv, String propertyName, String source, int value) {
        // t = new StringBuilder("...").append(value).toString()
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(StringBuilder.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn("Initialize property " + propertyName + " from " + source + ". Value: ");
        MethodInvocationUtil.invokeConstructor(mv, StringBuilder.class, String.class);
        mv.visitVarInsn(Opcodes.ALOAD, value);
        MethodInvocationUtil.invokeVirtual(mv, StringBuilder.class, "append", Object.class);
        MethodInvocationUtil.invokeVirtual(mv, StringBuilder.class, "toString");
        mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
        this.loggerDebug(mv, this.variableIndex);
    }

    private void initializeSessionProperty(MethodVisitor mv, PropertyDescriptor propertyDescriptor) {
        // this.setXXX(session.getAttribute("..."))
        mv.visitVarInsn(Opcodes.ILOAD, THIS);
        mv.visitVarInsn(Opcodes.ILOAD, this.session);
        mv.visitLdcInsn(propertyDescriptor.getName());
        MethodInvocationUtil.invokeInterface(mv, HttpSession.class, "getAttribute", String.class);
        if (this.debugMode) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
            this.logInitialization(mv, propertyDescriptor.getName(), "session", this.variableIndex);
        }
        Class<?> rawType = propertyDescriptor.getRawType();
        if (rawType.isPrimitive()) {
            Class<?> wrapperClass = primitiveToWrapper(rawType);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(wrapperClass));
            MethodInvocationUtil.invokeVirtual(mv, wrapperClass, rawType.getName() + "Value");
        } else {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(rawType));
        }
        Method method = propertyDescriptor.getAccessMethod();
        MethodInvocationUtil.invokeVirtual(mv, this.internalName, method.getReturnType(), method.getName(),
                                           method.getParameterTypes());
    }

    private void initializeNonSessionProperty(MethodVisitor mv, PropertyDescriptor propertyDescriptor) {
        Label nullLabel = new Label();
        Label endLabel = new Label();
        boolean isCookieProperty = propertyDescriptor.getCookieAnnotation() == null;

        if (isCookieProperty) {
            this.getValueFromParameter(mv, propertyDescriptor);
        } else {
            this.getValueFromCookie(mv, propertyDescriptor);
        }
        mv.visitInsn(Opcodes.DUP);
        int value = this.variableIndex++;
        mv.visitVarInsn(Opcodes.ASTORE, value);

        if (this.debugMode) {
            if (propertyDescriptor.getComponentType() == null) {
                this.logInitialization(mv, propertyDescriptor.getName(), isCookieProperty ? "cookie" : "parameter",
                                       value);
            } else {
                mv.visitInsn(Opcodes.DUP);
                MethodInvocationUtil.invokeStatic(mv, Arrays.class, "toString", Object[].class);
                mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
                this.logInitialization(mv, propertyDescriptor.getName(), isCookieProperty ? "cookie" : "parameter",
                                       this.variableIndex);
            }
        }

        // if (value == null) goto null
        mv.visitJumpInsn(Opcodes.IFNULL, nullLabel);

        mv.visitVarInsn(Opcodes.ILOAD, THIS);
        this.convertValue(mv, propertyDescriptor, value);
        Method method = propertyDescriptor.getAccessMethod();
        MethodInvocationUtil.invokeVirtual(mv, this.internalName, method.getReturnType(), method.getName(),
                                           method.getParameterTypes());

        if (propertyDescriptor.isRequired()) {
            // goto end
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
        }

        // null
        mv.visitLabel(nullLabel);

        if (propertyDescriptor.isRequired()) {
            if (this.debugMode) {
                this.loggerDebug(mv, "Required validation failure");
            }

            // throw new ValidationException("...")
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(ValidationException.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn("Missing required property " + propertyDescriptor.getName());
            MethodInvocationUtil.invokeConstructor(mv, ValidationException.class, String.class);
            mv.visitInsn(Opcodes.ATHROW);

            // end
            mv.visitLabel(endLabel);
        }
    }

    private void getValueFromParameter(MethodVisitor mv, PropertyDescriptor propertyDescriptor) {
        // value = req.getParameter(propertyName) or value = req.getParameters(propertyName)
        mv.visitVarInsn(Opcodes.ALOAD, REQ);
        mv.visitLdcInsn(propertyDescriptor.getName());
        if (propertyDescriptor.getComponentType() == null) {
            MethodInvocationUtil.invokeInterface(mv, HttpServletRequest.class, "getParameter", String.class);
        } else {
            MethodInvocationUtil.invokeInterface(mv, HttpServletRequest.class, "getParameterValues", String.class);
        }
    }

    private void getValueFromCookie(MethodVisitor mv, PropertyDescriptor propertyDescriptor) {
        if (this.cookieMap < 0) {
            this.getValueFromCookieArray(mv, propertyDescriptor);
        } else {
            this.getValueFromCookieMap(mv, propertyDescriptor);
        }
    }

    private void getValueFromCookieMap(MethodVisitor mv, PropertyDescriptor propertyDescriptor) {
        // cookieMap.get("xxx")
        mv.visitVarInsn(Opcodes.ALOAD, this.cookieMap);
        mv.visitLdcInsn(propertyDescriptor.getName());
        MethodInvocationUtil.invokeInterface(mv, Map.class, "get");
    }

    private void getValueFromCookieArray(MethodVisitor mv, PropertyDescriptor propertyDescriptor) {
        int cookieArray = this.variableIndex;
        int length = this.variableIndex + 1;
        int i = this.variableIndex + 2;
        int cookie = this.variableIndex + 3;

        // cookieArray = req.getCookies();
        mv.visitVarInsn(Opcodes.ALOAD, REQ);
        MethodInvocationUtil.invokeInterface(mv, HttpServletRequest.class, "getCookies");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ASTORE, cookieArray);

        // length = cookieArray.length;
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        mv.visitVarInsn(Opcodes.ISTORE, length);

        // i = 0
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, i);

        Label forTestLabel = new Label();
        // goto for_test
        mv.visitJumpInsn(Opcodes.GOTO, forTestLabel);

        Label forBodyLabel = new Label();
        // for_body
        mv.visitLabel(forBodyLabel);

        // cookie = cookieArray[i]
        mv.visitVarInsn(Opcodes.ALOAD, cookieArray);
        mv.visitVarInsn(Opcodes.ILOAD, i);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ASTORE, cookie);

        // if (!cookie.getName().equals("xxx")) goto not_equal
        MethodInvocationUtil.invokeVirtual(mv, Cookie.class, "getName");
        mv.visitLdcInsn(propertyDescriptor.getName());
        MethodInvocationUtil.invokeVirtual(mv, String.class, "equals", Object.class);
        Label notEqualsLabel = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, notEqualsLabel);

        // cookie.getValue()
        mv.visitVarInsn(Opcodes.ALOAD, cookie);
        MethodInvocationUtil.invokeVirtual(mv, Cookie.class, "getValue");

        // goto for_end
        Label forEndLabel = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, forEndLabel);

        // not_equal
        mv.visitLabel(notEqualsLabel);
        // i++
        mv.visitIincInsn(i, 1);

        // for_test
        mv.visitLabel(forTestLabel);

        // if (i < length) goto for_body
        mv.visitVarInsn(Opcodes.ILOAD, i);
        mv.visitVarInsn(Opcodes.ILOAD, length);
        mv.visitJumpInsn(Opcodes.IF_ICMPLT, forBodyLabel);

        // for_end
        mv.visitLabel(forEndLabel);
    }

    private void convertValue(MethodVisitor mv, PropertyDescriptor propertyDescriptor, int value) {
        if (propertyDescriptor.getComponentType() != null) {
            this.convertValueArray(mv, propertyDescriptor, value);
        } else {
            this.convertSingleValue(mv, propertyDescriptor, value);
        }
    }

    private void convertSingleValue(MethodVisitor mv, PropertyDescriptor propertyDescriptor, int value) {
        Class<?> rawType = propertyDescriptor.getRawType();
        if (rawType.isPrimitive()) {
            mv.visitVarInsn(Opcodes.ALOAD, value);
            // Integer.parseInt(value), Long.parseLong(value), etc
            MethodInvocationUtil.invokeStatic(mv, ActionProxyBuilder.primitiveToWrapper(rawType), "parse" +
                ActionUtil.capitalize(rawType.getName()), String.class);
        } else if (rawType.equals(String.class)) {
            // do nothing
        } else if (rawType.equals(Date.class)) {
            // fmt = new SimpleDateFormat()
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(SimpleDateFormat.class));
            mv.visitInsn(Opcodes.DUP);
            MethodInvocationUtil.invokeConstructor(mv, SimpleDateFormat.class);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);

            // fmt.setLenient(false)
            mv.visitInsn(Opcodes.ICONST_0);
            MethodInvocationUtil.invokeVirtual(mv, SimpleDateFormat.class, "setLenient", boolean.class);

            // fmt.parse(value)
            mv.visitVarInsn(Opcodes.ALOAD, this.variableIndex);
            mv.visitVarInsn(Opcodes.ALOAD, value);
            MethodInvocationUtil.invokeVirtual(mv, SimpleDateFormat.class, "parse", String.class);
        } else if (rawType.equals(File.class)) {
        } else {
            // new Integer(value), new BigInteger(value), etc
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(rawType));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, value);
            MethodInvocationUtil.invokeConstructor(mv, rawType, String.class);
        }
    }

    private void convertValueArray(MethodVisitor mv, PropertyDescriptor propertyDescriptor, int value) {
        int newvalue = this.variableIndex;
        int length = this.variableIndex + 1;
        int i = this.variableIndex + 2;
        int t = this.variableIndex + 3;
        this.variableIndex = t + 1;
        Class<?> rawType = propertyDescriptor.getRawType();
        Class<?> componentType = propertyDescriptor.getComponentType();

        // length = values.length
        mv.visitVarInsn(Opcodes.ALOAD, value);
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        mv.visitVarInsn(Opcodes.ISTORE, length);

        if (rawType.isArray()) {
            // newvalue = new xxx[length]
            mv.visitVarInsn(Opcodes.ILOAD, length);
            if (componentType.isPrimitive()) {
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);

                // TODO: ignore boolean array
                if (componentType.equals(boolean.class)) {
                    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
                } else if (componentType.equals(byte.class)) {
                    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
                } else if (componentType.equals(short.class)) {
                    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_SHORT);
                } else if (componentType.equals(int.class)) {
                    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
                } else if (componentType.equals(long.class)) {
                    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
                } else if (componentType.equals(float.class)) {
                    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
                } else if (componentType.equals(double.class)) {
                    mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
                } else {
                    throw new RuntimeException("Unexpected component type " + componentType);
                }
            } else {
                mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(componentType));
            }
        } else if (List.class.equals(rawType)) {
            // newvalue = new ArrayList(length)
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(ArrayList.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, length);
            MethodInvocationUtil.invokeConstructor(mv, ArrayList.class, int.class);
        } else if (Set.class.equals(rawType)) {
            // newvalue = new HashSet()
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(HashSet.class));
            mv.visitInsn(Opcodes.DUP);
            MethodInvocationUtil.invokeConstructor(mv, HashSet.class);
        } else {
            // Should not reach here
            throw new RuntimeException("Invalid containerClass " + rawType.getName());
        }
        mv.visitVarInsn(Opcodes.ASTORE, newvalue);

        // i = 0
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, i);

        // goto for_test
        Label forTestLabel = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, forTestLabel);

        // for_body:
        Label forBodyLabel = new Label();
        mv.visitLabel(forBodyLabel);

        // newvalue or newvalue[i]
        mv.visitVarInsn(Opcodes.ALOAD, newvalue);
        if (rawType.isArray()) {
            mv.visitVarInsn(Opcodes.ILOAD, i);
        }

        // convert(value[i])
        mv.visitVarInsn(Opcodes.ALOAD, value);
        mv.visitVarInsn(Opcodes.ILOAD, i);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitVarInsn(Opcodes.ASTORE, t);
        this.convertSingleValue(mv, propertyDescriptor, t);

        if (rawType.isArray()) {
            // newvalue[i] = top_of_stack
            if (componentType.equals(byte.class)) {
                mv.visitInsn(Opcodes.BASTORE);
            } else if (componentType.equals(short.class)) {
                mv.visitInsn(Opcodes.SASTORE);
            } else if (componentType.equals(int.class)) {
                mv.visitInsn(Opcodes.IASTORE);
            } else if (componentType.equals(long.class)) {
                mv.visitInsn(Opcodes.LASTORE);
            } else if (componentType.equals(float.class)) {
                mv.visitInsn(Opcodes.FASTORE);
            } else if (componentType.equals(double.class)) {
                mv.visitInsn(Opcodes.DASTORE);
            } else {
                mv.visitInsn(Opcodes.AASTORE);
            }
        } else {
            // newvalue.add(top_of_stack)
            MethodInvocationUtil.invokeInterface(mv, rawType, "add", Object.class);
            mv.visitInsn(Opcodes.POP);
        }

        // i++
        mv.visitIincInsn(i, 1);

        // for_test:
        mv.visitLabel(forTestLabel);

        // if (i < length) goto for_body
        mv.visitVarInsn(Opcodes.ILOAD, i);
        mv.visitVarInsn(Opcodes.ILOAD, length);
        mv.visitJumpInsn(Opcodes.IF_ICMPLT, forBodyLabel);

        mv.visitVarInsn(Opcodes.ALOAD, newvalue);

        this.variableIndex = newvalue;
    }

    private void buildFillAttributes(MethodVisitor mv) {
        for (PropertyDescriptor prop : this.actionDescriptor.getPropertyList()) {
            if (prop.getGetter() != null) {
                mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.REQ);
                mv.visitLdcInsn(prop.getName());
                mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.THIS);
                this.invokeSelfVirtual(mv, prop.getGetter().getName());
                if (prop.getElementClass().isPrimitive()) {
                    java.lang.reflect.Type targetType = ActionProxyBuilder.primitiveToWrapper(prop.getElementClass());
                    this.invokeStatic(mv, (Class<?>) targetType, "valueOf", prop.getElementClass());
                }
                if (prop.isSetToSession()) {
                    mv.visitVarInsn(Opcodes.ASTORE, cn.edu.zju.acm.mvc.control.actionproxy.VAR3);
                    mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.VAR3);
                }
                ActionProxyBuilder.this.invokeInterface(mv, HttpServletRequest.class, "setAttribute", String.class,
                                                        Object.class);
                if (prop.isSetToSession()) {
                    mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.SESSION);
                    mv.visitLdcInsn(prop.getName());
                    mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.VAR3);
                    ActionProxyBuilder.this.invokeInterface(mv, HttpSession.class, "setAttribute", String.class,
                                                            Object.class);
                }
            }
        }
    }

    private void buildAddCookies(MethodVisitor mv, Label endLabel) {
        mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.THIS);
        mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "outputCookies", Type.getDescriptor(List.class));
        mv.visitJumpInsn(Opcodes.IFNULL, endLabel);

        // i = this.outputCookies.iterator()
        mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.THIS);
        mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "outputCookies", Type.getDescriptor(List.class));
        this.invokeInterface(mv, List.class, "iterator");
        mv.visitVarInsn(Opcodes.ASTORE, cn.edu.zju.acm.mvc.control.actionproxy.I);

        // goto for_test
        Label forTestLabel = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, forTestLabel);

        // for_body:
        Label forBodyLabel = new Label();
        mv.visitLabel(forBodyLabel);

        // resp.addCookie((Cookie)i.next())
        mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.RESP);
        mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.I);
        this.invokeInterface(mv, Iterator.class, "next");
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Cookie.class));
        this.invokeInterface(mv, HttpServletResponse.class, "addCookie", Cookie.class);
        mv.visitLabel(forTestLabel);
        mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.I);
        this.invokeInterface(mv, Iterator.class, "hasNext");
        mv.visitJumpInsn(Opcodes.IFNE, forBodyLabel);
    }

    private void convertResult(MethodVisitor mv) {
        Map<String, Result> resultMap = this.actionDescriptor.getResultMap();

        for (Map.Entry<String, Result> entry : resultMap.entrySet()) {
            // if (!"...".equals(var2)) goto not_equal
            mv.visitLdcInsn(entry.getKey());
            mv.visitVarInsn(Opcodes.ALOAD, RESULT);
            MethodInvocationUtil.invokeVirtual(mv, String.class, "equals", Object.class);
            Label notEqualsLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, notEqualsLabel);

            // result = "...";
            mv.visitLdcInsn(pair.getSecond());
            mv.visitVarInsn(Opcodes.ASTORE, cn.edu.zju.acm.mvc.control.actionproxy.VAR1);

            // goto end_try_catch
            mv.visitJumpInsn(Opcodes.GOTO, endTryCatchLabel);

            // not_equal
            mv.visitLabel(notEqualsLabel);
        }

        // if (result != null) goto result_not_null
        mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.VAR1);
        Label resultNotNullLabel = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, resultNotNullLabel);

        // throw new InvalidResultException(var2)
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(InvalidResultException.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.VAR2);
        this.invokeConstructor(mv, InvalidResultException.class, String.class);
        mv.visitInsn(Opcodes.ATHROW);

        // result_not_null:
        mv.visitLabel(resultNotNullLabel);

        this.buildFillAttributes(mv);

        this.buildAddCookies(mv, endTryCatchLabel);

        // return result
        mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.VAR1);
        mv.visitInsn(Opcodes.ARETURN);
    }

    public void getField(MethodVisitor mv, Class<?> fieldClass, String fieldName) {
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        mv.visitFieldInsn(Opcodes.GETFIELD, ActionProxyBuilder.this.internalName, fieldName,
                          Type.getDescriptor(fieldClass));
    }

    private void loggerDebug(MethodVisitor mv, String message) {
        // this.logger.debug(message);
        this.getField(mv, Logger.class, "logger");
        mv.visitLdcInsn(message);
        MethodInvocationUtil.invokeVirtual(mv, Logger.class, "debug", String.class);
    }

    private void loggerDebug(MethodVisitor mv, int message) {
        // this.logger.debug(message);
        this.getField(mv, Logger.class, "logger");
        mv.visitVarInsn(Opcodes.ALOAD, message);
        MethodInvocationUtil.invokeVirtual(mv, Logger.class, "debug", String.class);
    }

    private void loggerDebug(MethodVisitor mv, String message, int e) {
        // this.logger.debug(message, e);
        this.getField(mv, Logger.class, "logger");
        mv.visitLdcInsn(message);
        mv.visitVarInsn(Opcodes.ALOAD, e);
        MethodInvocationUtil.invokeVirtual(mv, Logger.class, "debug", String.class, Object.class);
    }

    private void initializeSession(MethodVisitor mv) {
        boolean requireSession = false;
        for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getInputPropertyMap().values()) {
            if (propertyDescriptor.isSessionVariable()) {
                requireSession = true;
                break;
            }
        }
        if (!requireSession) {
            for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getOutputPropertyMap().values()) {
                if (propertyDescriptor.isSessionVariable()) {
                    requireSession = true;
                    break;
                }
            }
        }
        if (requireSession) {
            // HttpSession session = req.getSession();
            this.session = this.variable++;
            mv.visitVarInsn(Opcodes.ALOAD, cn.edu.zju.acm.mvc.control.actionproxy.REQ);
            MethodInvocationUtil.invokeInterface(mv, HttpServletRequest.class, "getSession");
            mv.visitVarInsn(Opcodes.ASTORE, this.session);
        }
    }

    private class ExecuteMethodGenerator {

        private MethodVisitor mv;

        public ExecuteMethodGenerator(ClassWriter cw) {
            mv =
                    cw.visitMethod(Opcodes.ACC_PUBLIC, "execute",
                                   MethodInvocationUtil.getMethodDescriptor(String.class, HttpServletRequest.class,
                                                                            HttpServletResponse.class), null,
                                   new String[] {Type.getInternalName(Exception.class)});

        }

        public void execute() {
            mv.visitCode();

            mv.visitEnd();
        }
    }

    public static Class<?> primitiveToWrapper(java.lang.reflect.Type type) {
        if (int.class.equals(type)) {
            return Integer.class;
        } else if (boolean.class.equals(type)) {
            return Boolean.class;
        } else if (long.class.equals(type)) {
            return Long.class;
        } else if (double.class.equals(type)) {
            return Double.class;
        } else if (byte.class.equals(type)) {
            return Byte.class;
        } else if (short.class.equals(type)) {
            return Short.class;
        } else if (float.class.equals(type)) {
            return Float.class;
        }
        return null;
    }
}
