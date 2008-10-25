
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
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

import cn.edu.zju.acm.mvc.control.annotation.Header;
import cn.edu.zju.acm.mvc.control.annotation.OneException;
import cn.edu.zju.acm.mvc.control.annotation.Result;
import cn.edu.zju.acm.mvc.control.annotation.ResultType;

public class ActionProxyBuilder {

    private static ActionProxyClassLoader actionProxyClassLoader = new ActionProxyClassLoader();

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

    private byte[] classContent;

    private String defaultDateFormat;

    public Class<? extends ActionProxy> build(ActionDescriptor actionDescriptor, String defaultDateFormat,
                                              boolean debugMode) {
        this.actionDescriptor = actionDescriptor;
        this.defaultDateFormat = defaultDateFormat;
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
        classContent = this.cw.toByteArray();
        return ActionProxyBuilder.actionProxyClassLoader
                                                        .defineClass(this.actionClass.getName() + "Proxy", classContent);
    }

    byte[] getClassContent() {
        return this.classContent;
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
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void buildExecute() {
        // public String execute(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        MethodVisitor mv =
                this.cw.visitMethod(Opcodes.ACC_PUBLIC, "execute",
                                    MethodInvocationUtil.getMethodDescriptor(void.class, HttpServletRequest.class,
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

        this.dispatchResult(mv);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
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
            this.session = this.variableIndex++;
            mv.visitVarInsn(Opcodes.ALOAD, REQ);
            MethodInvocationUtil.invokeInterface(mv, HttpServletRequest.class, "getSession");
            mv.visitVarInsn(Opcodes.ASTORE, this.session);
        }
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

        Label noErrorLabel = new Label();

        // if (this.getFieldErrors().size() == 0) goto no_error
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        MethodInvocationUtil.invokeVirtual(mv, Action.class, "getFieldErrors");
        MethodInvocationUtil.invokeInterface(mv, List.class, "size");
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, noErrorLabel);

        // throw new FieldInitializationErrorException();
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(FieldInitializationErrorException.class));
        mv.visitInsn(Opcodes.DUP);
        MethodInvocationUtil.invokeConstructor(mv, FieldInitializationErrorException.class);
        mv.visitInsn(Opcodes.ATHROW);

        // no_error
        mv.visitLabel(noErrorLabel);
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
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        mv.visitVarInsn(Opcodes.ALOAD, this.session);
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
        MethodInvocationUtil.invokeVirtual(mv, this.actionClass, method.getReturnType(), method.getName(),
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

        Label startTryCatchLabel = new Label();
        Label endTryCatchLabel = new Label();
        List<Label> labelList = new ArrayList<Label>();
        List<Class<? extends Exception>> exceptionList = propertyDescriptor.getConversionExceptionClasses();
        for (Class<? extends Exception> exception : exceptionList) {
            Label label = new Label();
            labelList.add(label);
            mv.visitTryCatchBlock(startTryCatchLabel, labelList.get(0), label, Type.getInternalName(exception));
        }
        // start_try_catch:
        mv.visitLabel(startTryCatchLabel);

        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        this.convertValue(mv, propertyDescriptor, value);
        Method method = propertyDescriptor.getAccessMethod();
        MethodInvocationUtil.invokeVirtual(mv, this.actionClass, method.getReturnType(), method.getName(),
                                           method.getParameterTypes());

        for (int i = 0; i < exceptionList.size(); ++i) {
            // goto end_try_catch
            mv.visitJumpInsn(Opcodes.GOTO, endTryCatchLabel);

            // catch(XXXException e)
            mv.visitLabel(labelList.get(i));
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);

            if (this.debugMode) {
                loggerDebug(mv, "Catch exception " + exceptionList.get(i).getName(), this.variableIndex);
            }
            if (propertyDescriptor.getComponentType() == null) {
                this.addFieldError(mv, propertyDescriptor.getName(), propertyDescriptor.getConversionErrorMessageKey(),
                                   value);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, value);
                MethodInvocationUtil.invokeStatic(mv, Arrays.class, "toString", Object[].class);
                mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
                this.addFieldError(mv, propertyDescriptor.getName(), propertyDescriptor.getConversionErrorMessageKey(),
                                   this.variableIndex);
            }
        }
        // end_try_catch:
        mv.visitLabel(endTryCatchLabel);

        if (propertyDescriptor.getRequiredAnnotation() != null) {
            // goto end
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
        }

        // null
        mv.visitLabel(nullLabel);

        if (propertyDescriptor.getRequiredAnnotation() != null) {
            if (this.debugMode) {
                this.loggerDebug(mv, "Required validation failure");
            }

            this.addFieldError(mv, propertyDescriptor.getName(), propertyDescriptor.getRequiredAnnotation().message());

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
        Class<?> rawType = propertyDescriptor.getComponentType();
        if (rawType == null) {
            rawType = propertyDescriptor.getRawType();
        }
        if (rawType.isPrimitive()) {
            if (boolean.class.equals(rawType)) {
                mv.visitInsn(Opcodes.ICONST_1);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, value);
                // Integer.parseInt(value), Long.parseLong(value), etc
                MethodInvocationUtil.invokeStatic(mv, ActionProxyBuilder.primitiveToWrapper(rawType), "parse" +
                    ActionUtil.capitalize(rawType.getName()), String.class);
            }

        } else if (rawType.equals(String.class)) {
            // do nothing
            mv.visitVarInsn(Opcodes.ALOAD, value);
        } else if (rawType.equals(Date.class)) {
            if (this.defaultDateFormat == null) {
                // fmt = DateFormat.getDateInstance(DateFormat.SHORT);
                this.loadIntConstant(mv, DateFormat.SHORT);
                MethodInvocationUtil.invokeStatic(mv, DateFormat.class, "getDateInstance", int.class);
            } else {
                // fmt = new SimpleDateFormat(defaultDateFormat);
                mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(SimpleDateFormat.class));
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(this.defaultDateFormat);
                MethodInvocationUtil.invokeConstructor(mv, SimpleDateFormat.class, String.class);
            }

            // fmt.setLenient(false)
            mv.visitInsn(Opcodes.DUP);
            mv.visitInsn(Opcodes.ICONST_0);
            MethodInvocationUtil.invokeVirtual(mv, DateFormat.class, "setLenient", boolean.class);

            // fmt.parse(value)
            mv.visitVarInsn(Opcodes.ALOAD, value);
            MethodInvocationUtil.invokeVirtual(mv, DateFormat.class, "parse", String.class);
        } else if (rawType.equals(File.class)) {
            // TODO
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else {
            // new Integer(value), new BigInteger(value), etc
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(rawType));
            mv.visitInsn(Opcodes.DUP);
            if (Boolean.class.equals(rawType)) {
                mv.visitLdcInsn("true");
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, value);
            }
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
                if (componentType.equals(byte.class)) {
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

    private void addCookie(MethodVisitor mv, PropertyDescriptor propertyDescriptor) {
        Class<?> rawType = propertyDescriptor.getRawType();
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        Method method = propertyDescriptor.getAccessMethod();
        MethodInvocationUtil.invokeVirtual(mv, this.actionClass, method.getReturnType(), method.getName());
        Label nullLabel = new Label();
        if (rawType.isPrimitive()) {
            MethodInvocationUtil.invokeStatic(mv, primitiveToWrapper(rawType), "toString", rawType);
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
        } else {
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
            mv.visitJumpInsn(Opcodes.IFNULL, nullLabel);

            if (rawType.equals(Date.class)) {
                if (this.defaultDateFormat == null) {
                    // fmt = DateFormat.getDateInstance(DateFormat.SHORT);
                    this.loadIntConstant(mv, DateFormat.SHORT);
                    MethodInvocationUtil.invokeStatic(mv, DateFormat.class, "getDateInstance", int.class);
                } else {
                    // fmt = new SimpleDateFormat(defaultDateFormat);
                    mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(SimpleDateFormat.class));
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitLdcInsn(this.defaultDateFormat);
                    MethodInvocationUtil.invokeConstructor(mv, SimpleDateFormat.class, String.class);
                }
                mv.visitVarInsn(Opcodes.ALOAD, this.variableIndex);
                MethodInvocationUtil.invokeVirtual(mv, DateFormat.class, "format", Date.class);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, this.variableIndex);
                MethodInvocationUtil.invokeVirtual(mv, rawType, "toString");
            }
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
        }

        // resp.addCookie(cookie)
        mv.visitVarInsn(Opcodes.ALOAD, RESP);

        // new Cookie("name", "value");
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(Cookie.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(propertyDescriptor.getName());
        mv.visitVarInsn(Opcodes.ALOAD, this.variableIndex);
        MethodInvocationUtil.invokeConstructor(mv, Cookie.class);

        cn.edu.zju.acm.mvc.control.annotation.Cookie cookie = propertyDescriptor.getCookieAnnotation();
        if (cookie.domain().length() > 0) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(cookie.domain());
            MethodInvocationUtil.invokeVirtual(mv, Cookie.class, "setDomain", String.class);
        }
        if (cookie.maxAge() >= 0) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(cookie.maxAge());
            MethodInvocationUtil.invokeVirtual(mv, Cookie.class, "setMaxAge", int.class);
        }
        if (cookie.path().length() > 0) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(cookie.path());
            MethodInvocationUtil.invokeVirtual(mv, Cookie.class, "setPath", String.class);
        }
        if (cookie.secure()) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitInsn(Opcodes.ICONST_1);
            MethodInvocationUtil.invokeVirtual(mv, Cookie.class, "setSecure", boolean.class);
        }
        if (cookie.version() >= 0) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(cookie.version());
            MethodInvocationUtil.invokeVirtual(mv, Cookie.class, "setVersion", int.class);
        }

        MethodInvocationUtil.invokeInterface(mv, HttpServletResponse.class, "addCookie", Cookie.class);

        if (!rawType.isPrimitive()) {
            // null
            mv.visitLabel(nullLabel);
        }
    }

    private void setAttribute(MethodVisitor mv, PropertyDescriptor propertyDescriptor, int container) {
        Class<?> rawType = propertyDescriptor.getRawType();
        // container.setAttribute("name", value);
        mv.visitVarInsn(Opcodes.ALOAD, container);

        if (rawType.isPrimitive()) {
            // new Integer(value), new Long(value), etc
            Class<?> wrapperClass = primitiveToWrapper(rawType);
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(wrapperClass));
            mv.visitInsn(Opcodes.DUP);
        }

        // this.getXXX();
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        Method method = propertyDescriptor.getAccessMethod();
        MethodInvocationUtil.invokeVirtual(mv, this.actionClass, method.getReturnType(), method.getName());

        if (rawType.isPrimitive()) {
            MethodInvocationUtil.invokeConstructor(mv, Cookie.class);
        }

        mv.visitLdcInsn(propertyDescriptor.getName());
        MethodInvocationUtil.invokeInterface(mv, HttpSession.class, "setAttribute", String.class, Object.class);
    }

    private void setSessionAttributes(MethodVisitor mv) {
        for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getOutputPropertyMap().values()) {
            if (propertyDescriptor.isSessionVariable()) {
                this.setAttribute(mv, propertyDescriptor, this.session);
            }
        }
    }

    private void addCookies(MethodVisitor mv) {
        for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getOutputPropertyMap().values()) {
            if (propertyDescriptor.getCookieAnnotation() != null) {
                this.addCookie(mv, propertyDescriptor);
            }
        }
    }

    private void setRequestAttributes(MethodVisitor mv) {
        for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getOutputPropertyMap().values()) {
            this.setAttribute(mv, propertyDescriptor, REQ);
        }
    }

    private void dispatchResult(MethodVisitor mv) {
        Map<String, Result> resultMap = this.actionDescriptor.getResultMap();
        Set<ResultType> resultTypeSet = new HashSet<ResultType>();
        for (Result result : resultMap.values()) {
            resultTypeSet.add(result.type());
        }
        int resultType = resultTypeSet.size() == 1 ? -1 : this.variableIndex++;

        Label resultNotNullLabel = new Label();
        for (Map.Entry<String, Result> entry : resultMap.entrySet()) {
            // if (!"...".equals(var2)) goto not_equal
            mv.visitLdcInsn(entry.getKey());
            mv.visitVarInsn(Opcodes.ALOAD, RESULT);
            MethodInvocationUtil.invokeVirtual(mv, String.class, "equals", Object.class);
            Label notEqualsLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, notEqualsLabel);

            Result result = entry.getValue();
            if (resultTypeSet.size() > 1) {
                // resultType = ...;
                mv.visitLdcInsn(result.type().ordinal());
                mv.visitVarInsn(Opcodes.ISTORE, resultType);
            }

            if (result.type().equals(ResultType.Raw)) {
                // result = this.getXXX();
                mv.visitVarInsn(Opcodes.ALOAD, THIS);
                Method method = this.actionDescriptor.getOutputPropertyMap().get(result.value()).getAccessMethod();
                MethodInvocationUtil.invokeVirtual(mv, this.actionClass, method.getReturnType(), method.getName());
            } else {
                // result = "value";
                mv.visitLdcInsn(result.value());
            }
            mv.visitVarInsn(Opcodes.ASTORE, RESULT);

            for (Header header : result.headers()) {
                // resp.addHeader("name", "value");
                mv.visitVarInsn(Opcodes.ALOAD, RESP);
                mv.visitLdcInsn(header.name());
                mv.visitLdcInsn(header.value());
                MethodInvocationUtil.invokeInterface(mv, HttpServletResponse.class, "addHeader", String.class,
                                                     String.class);
            }

            // goto result_not_null
            mv.visitJumpInsn(Opcodes.GOTO, resultNotNullLabel);

            // not_equal
            mv.visitLabel(notEqualsLabel);
        }

        // throw new InvalidResultException(var2)
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(InvalidResultException.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, RESULT);
        MethodInvocationUtil.invokeConstructor(mv, InvalidResultException.class, String.class);
        mv.visitInsn(Opcodes.ATHROW);

        // result_not_null:
        mv.visitLabel(resultNotNullLabel);

        this.setSessionAttributes(mv);
        this.addCookies(mv);

        List<Label> labelList = new ArrayList<Label>();
        for (ResultType rt : new ResultType[] {ResultType.Jsp, ResultType.ZView, ResultType.Redirect, ResultType.Raw}) {
            if (resultTypeSet.contains(rt)) {
                if (labelList.size() > 0) {
                    mv.visitLabel(labelList.get(labelList.size() - 1));
                }
                if (resultTypeSet.size() > 1) {
                    mv.visitVarInsn(Opcodes.ILOAD, resultType);
                    mv.visitLdcInsn(rt.ordinal());
                    Label label = new Label();
                    mv.visitJumpInsn(Opcodes.IFNE, label);
                }
                if (rt.equals(ResultType.Jsp)) {
                    this.dispatchJspResult(mv);
                } else if (rt.equals(ResultType.ZView)) {
                    this.dispatchZViewResult(mv);
                } else if (rt.equals(ResultType.Redirect)) {
                    this.dispatchRedirectResult(mv);
                } else {
                    this.dispatchRawResult(mv);
                }
                resultTypeSet.remove(rt);
            }
        }
    }

    private void dispatchJspResult(MethodVisitor mv) {
        this.setRequestAttributes(mv);

        // req.getRequestDispatcher(result).forward(req, resp);
        mv.visitVarInsn(Opcodes.ALOAD, REQ);
        mv.visitVarInsn(Opcodes.ALOAD, RESULT);
        MethodInvocationUtil.invokeInterface(mv, HttpServletRequest.class, "getRequestDispatcher", String.class);
        mv.visitVarInsn(Opcodes.ALOAD, REQ);
        mv.visitVarInsn(Opcodes.ALOAD, RESP);
        MethodInvocationUtil.invokeInterface(mv, RequestDispatcher.class, "forward", HttpServletRequest.class,
                                             HttpServletResponse.class);
    }

    private void dispatchZViewResult(MethodVisitor mv) {
        // TODO
    }

    private void dispatchRedirectResult(MethodVisitor mv) {
        // resp.sendRedirect(result);
        mv.visitVarInsn(Opcodes.ALOAD, RESP);
        mv.visitVarInsn(Opcodes.ALOAD, RESULT);
        MethodInvocationUtil.invokeInterface(mv, HttpServletResponse.class, "sendRedirect", String.class);
    }

    private void dispatchRawResult(MethodVisitor mv) {
        int b = this.variableIndex;
        int out = this.variableIndex + 1;
        int length = this.variableIndex + 2;
        Label startLabel = new Label();
        Label endLabel = new Label();

        // byte[] b = new byte[4096];
        mv.visitLdcInsn(4096);
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
        mv.visitVarInsn(Opcodes.ASTORE, b);

        // out = resp.getOutputStream();
        mv.visitVarInsn(Opcodes.ALOAD, RESP);
        MethodInvocationUtil.invokeInterface(mv, HttpServletResponse.class, "getOutputStream");
        mv.visitVarInsn(Opcodes.ASTORE, out);

        // start
        mv.visitLabel(startLabel);

        // length = result.read(b);
        mv.visitVarInsn(Opcodes.ALOAD, RESULT);
        mv.visitVarInsn(Opcodes.ALOAD, b);
        MethodInvocationUtil.invokeVirtual(mv, InputStream.class, "read", byte[].class);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ISTORE, length);

        // if (length < 0) goto end
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitJumpInsn(Opcodes.IFLT, endLabel);

        // out.write(b, 0, length);
        mv.visitVarInsn(Opcodes.ALOAD, out);
        mv.visitVarInsn(Opcodes.ALOAD, b);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ILOAD, length);
        MethodInvocationUtil.invokeVirtual(mv, ServletOutputStream.class, "write", byte[].class, int.class, int.class);

        // goto start;
        mv.visitJumpInsn(Opcodes.GOTO, startLabel);

        // end
        mv.visitLabel(endLabel);

        // out.close();
        mv.visitVarInsn(Opcodes.ALOAD, out);
        MethodInvocationUtil.invokeVirtual(mv, ServletOutputStream.class, "close");
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
        MethodInvocationUtil.invokeVirtual(mv, Logger.class, "debug", Object.class);
    }

    private void loggerDebug(MethodVisitor mv, int message) {
        // this.logger.debug(message);
        this.getField(mv, Logger.class, "logger");
        mv.visitVarInsn(Opcodes.ALOAD, message);
        MethodInvocationUtil.invokeVirtual(mv, Logger.class, "debug", Object.class);
    }

    private void loggerDebug(MethodVisitor mv, String message, int e) {
        // this.logger.debug(message, e);
        this.getField(mv, Logger.class, "logger");
        mv.visitLdcInsn(message);
        mv.visitVarInsn(Opcodes.ALOAD, e);
        MethodInvocationUtil.invokeVirtual(mv, Logger.class, "debug", Object.class, Throwable.class);
    }

    private void addFieldError(MethodVisitor mv, String name, String messageKey, int... arguments) {
        // this.addFieldError("name", "messageKey");
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        mv.visitLdcInsn(name);
        mv.visitLdcInsn(messageKey);
        this.loadIntConstant(mv, arguments.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
        for (int i = 0; i < arguments.length; ++i) {
            mv.visitInsn(Opcodes.DUP);
            this.loadIntConstant(mv, i);
            mv.visitVarInsn(Opcodes.ALOAD, arguments[i]);
            mv.visitInsn(Opcodes.AASTORE);
        }
        MethodInvocationUtil.invokeVirtual(mv, Action.class, void.class, "addFieldError", String.class, String.class,
                                           String[].class);
    }

    private void loadIntConstant(MethodVisitor mv, int value) {
        switch (value) {
            case -1:
                mv.visitInsn(Opcodes.ICONST_M1);
                break;
            case 0:
                mv.visitInsn(Opcodes.ICONST_0);
                break;
            case 1:
                mv.visitInsn(Opcodes.ICONST_1);
                break;
            case 2:
                mv.visitInsn(Opcodes.ICONST_2);
                break;
            case 3:
                mv.visitInsn(Opcodes.ICONST_3);
                break;
            case 4:
                mv.visitInsn(Opcodes.ICONST_4);
                break;
            case 5:
                mv.visitInsn(Opcodes.ICONST_5);
                break;
            default:
                mv.visitLdcInsn(value);
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
