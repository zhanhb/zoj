
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import cn.edu.zju.acm.mvc.control.annotation.validator.DoubleRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.IntRangeValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringLengthValidator;
import cn.edu.zju.acm.mvc.control.annotation.validator.StringPatternValidator;

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
        this.buildStaticInitializer();
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

    private void buildStaticInitializer() {
        boolean created = false;
        MethodVisitor mv = null;
        for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getInputPropertyMap().values()) {
            for (Annotation annotation : propertyDescriptor.getValidators()) {
                if (annotation instanceof StringPatternValidator) {
                    if (!created) {
                        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                        mv.visitCode();
                        created = true;
                    }
                    String fieldName = "p_" + propertyDescriptor.getName();
                    cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, fieldName,
                                  Type.getDescriptor(Pattern.class), null, null).visitEnd();
                    mv.visitLdcInsn(((StringPatternValidator) annotation).pattern());
                    MethodInvocationUtil.invokeStatic(mv, Pattern.class, "compile", String.class);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, this.internalName, fieldName,
                                      Type.getDescriptor(Pattern.class));
                }
            }
        }
        if (created) {
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
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

        if (this.debugMode) {
            this.loggerDebug(mv, "Leave " + ActionProxyBuilder.this.actionClass.getName());
        }

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
        if (this.debugMode) {
            this.loggerDebug(mv, "\tInitializing properties");
        }

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

        if (this.debugMode) {
            this.loggerDebug(mv, "\tInitialization failed");
        }

        // throw new FieldInitializationErrorException();
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(FieldInitializationErrorException.class));
        mv.visitInsn(Opcodes.DUP);
        MethodInvocationUtil.invokeConstructor(mv, FieldInitializationErrorException.class);
        mv.visitInsn(Opcodes.ATHROW);

        // no_error
        mv.visitLabel(noErrorLabel);
        if (this.debugMode) {
            this.loggerDebug(mv, "\tInitialization done");
        }
    }

    private void logInitialization(MethodVisitor mv, String propertyName, String source, int value) {
        this.concatString(mv,
                          new Object[] {"\t\tInitialize property " + propertyName + " from " + source + ". Value: ",
                                        new LocalVariable(value)});
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
        boolean isCookieProperty = propertyDescriptor.getCookieAnnotation() != null;

        if (isCookieProperty) {
            this.getValueFromCookie(mv, propertyDescriptor);
        } else {
            this.getValueFromParameter(mv, propertyDescriptor);
        }
        mv.visitInsn(Opcodes.DUP);
        int value = this.variableIndex++;
        mv.visitVarInsn(Opcodes.ASTORE, value);

        // if (value == null) goto null
        mv.visitJumpInsn(Opcodes.IFNULL, nullLabel);

        if (this.debugMode) {
            if (propertyDescriptor.getComponentType() == null) {
                this.logInitialization(mv, propertyDescriptor.getName(), isCookieProperty ? "cookie" : "parameter",
                                       value);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, value);
                MethodInvocationUtil.invokeStatic(mv, Arrays.class, "toString", Object[].class);
                mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
                this.logInitialization(mv, propertyDescriptor.getName(), isCookieProperty ? "cookie" : "parameter",
                                       this.variableIndex);
            }
        }

        Label startTryCatchLabel = new Label();
        Label endTryCatchLabel = new Label();
        List<Label> labelList = new ArrayList<Label>();
        List<Class<? extends Exception>> exceptionList =
                new ArrayList<Class<? extends Exception>>(propertyDescriptor.getConversionExceptionClasses());
        if (propertyDescriptor.getValidators().size() > 0) {
            exceptionList.add(ValidationException.class);
        }
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
            mv.visitInsn(Opcodes.POP);
            if (!exceptionList.get(i).equals(ValidationException.class)) {
                if (this.debugMode) {
                    loggerDebug(mv, "\t\t\tCatch exception " + exceptionList.get(i).getName());
                }

                if (propertyDescriptor.getComponentType() == null) {
                    this.addFieldError(mv, propertyDescriptor.getName(),
                                       propertyDescriptor.getConversionErrorMessageKey(), new LocalVariable(value));
                } else {
                    mv.visitVarInsn(Opcodes.ALOAD, value);
                    MethodInvocationUtil.invokeStatic(mv, Arrays.class, "toString", Object[].class);
                    mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
                    this.addFieldError(mv, propertyDescriptor.getName(),
                                       propertyDescriptor.getConversionErrorMessageKey(),
                                       new LocalVariable(this.variableIndex));
                }
            }
        }
        // end_try_catch:
        mv.visitLabel(endTryCatchLabel);

        if (propertyDescriptor.getRequiredAnnotation() != null || this.debugMode) {
            // goto end
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);
        }

        // null
        mv.visitLabel(nullLabel);

        if (this.debugMode) {
            this.loggerDebug(mv, "\t\tNo input for property " + propertyDescriptor.getName());
        }

        if (propertyDescriptor.getRequiredAnnotation() != null) {
            if (this.debugMode) {
                this.loggerDebug(mv, "\t\t\tRequired validation failure");
            }

            this.addFieldError(mv, propertyDescriptor.getName(), propertyDescriptor.getRequiredAnnotation().message());
        }
        
        // end
        mv.visitLabel(endLabel);
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
        mv.visitInsn(Opcodes.NULL);

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
        this.validate(mv, propertyDescriptor);
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

    private void validate(MethodVisitor mv, PropertyDescriptor propertyDescriptor) {
        for (Annotation annotation : propertyDescriptor.getValidators()) {
            Class<?> type = propertyDescriptor.getComponentType();
            if (type == null) {
                type = propertyDescriptor.getRawType();
            }
            Label successLabel = new Label();
            Label failureLabel = new Label();
            String message;
            ArrayList<Object> arguments = new ArrayList<Object>();
            arguments.add(new LocalVariable(this.variableIndex));
            if (annotation instanceof IntRangeValidator) {
                IntRangeValidator validator = (IntRangeValidator) annotation;
                this.validateIntRange(mv, validator, failureLabel);
                message = validator.message();
                arguments.add(validator.min());
                arguments.add(validator.max());
            } else if (annotation instanceof DoubleRangeValidator) {
                DoubleRangeValidator validator = (DoubleRangeValidator) annotation;
                this.validateDoubleRange(mv, validator, failureLabel);
                message = validator.message();
                arguments.add(validator.min());
                arguments.add(validator.max());
            } else if (annotation instanceof StringLengthValidator) {
                StringLengthValidator validator = (StringLengthValidator) annotation;
                this.validateStringLength(mv, validator, failureLabel);
                message = validator.message();
                arguments.add(validator.min());
                arguments.add(validator.max());
            } else if (annotation instanceof StringPatternValidator) {
                StringPatternValidator validator = (StringPatternValidator) annotation;
                this.validateStringPattern(mv, propertyDescriptor.getName(), validator, failureLabel);
                message = validator.message();
                arguments.add(validator.pattern());
            } else {
                continue;
            }
            mv.visitJumpInsn(Opcodes.GOTO, successLabel);

            mv.visitLabel(failureLabel);
            if (type.equals(int.class)) {
                MethodInvocationUtil.invokeStatic(mv, Integer.class, "toString", int.class);
            } else if (type.equals(long.class)) {
                MethodInvocationUtil.invokeStatic(mv, Long.class, "toString", long.class);
            } else if (type.equals(double.class)) {
                MethodInvocationUtil.invokeStatic(mv, Double.class, "toString", double.class);
            }
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);

            if (this.debugMode) {
                this.loggerDebug(mv, "\t\t\tValidation failed");
            }

            this.addFieldError(mv, propertyDescriptor.getName(), message, arguments.toArray(new Object[0]));
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(ValidationException.class));
            mv.visitInsn(Opcodes.DUP);
            MethodInvocationUtil.invokeConstructor(mv, ValidationException.class);
            mv.visitInsn(Opcodes.ATHROW);

            mv.visitLabel(successLabel);
        }
    }

    private void validateStringPattern(MethodVisitor mv, String propertyName, StringPatternValidator validator,
                                       Label failureLabel) {
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(Opcodes.GETSTATIC, this.internalName, "p_" + propertyName, Type.getDescriptor(Pattern.class));
        mv.visitInsn(Opcodes.SWAP);
        MethodInvocationUtil.invokeVirtual(mv, Pattern.class, "matcher", CharSequence.class);
        MethodInvocationUtil.invokeVirtual(mv, Matcher.class, "matches");
        mv.visitJumpInsn(Opcodes.IFEQ, failureLabel);
    }

    private void validateStringLength(MethodVisitor mv, StringLengthValidator validator, Label failureLabel) {
        mv.visitInsn(Opcodes.DUP);
        MethodInvocationUtil.invokeVirtual(mv, String.class, "length");
        mv.visitVarInsn(Opcodes.ISTORE, this.variableIndex);
        if (validator.min() > Integer.MIN_VALUE) {
            mv.visitVarInsn(Opcodes.ILOAD, this.variableIndex);
            this.loadIntConstant(mv, validator.min());
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, failureLabel);
        }

        if (validator.max() < Integer.MAX_VALUE) {
            mv.visitVarInsn(Opcodes.ILOAD, this.variableIndex);
            this.loadIntConstant(mv, validator.max());
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, failureLabel);
        }
    }

    private void validateDoubleRange(MethodVisitor mv, DoubleRangeValidator validator, Label failureLabel) {
        if (validator.min() > Double.MIN_VALUE) {
            mv.visitInsn(Opcodes.DUP2);
            this.loadDoubleConstant(mv, validator.min());
            mv.visitInsn(Opcodes.DCMPG);
            mv.visitJumpInsn(Opcodes.IFLT, failureLabel);
        }
        if (validator.max() < Double.MAX_VALUE) {
            mv.visitInsn(Opcodes.DUP2);
            this.loadDoubleConstant(mv, validator.max());
            mv.visitInsn(Opcodes.DCMPG);
            mv.visitJumpInsn(Opcodes.IFGT, failureLabel);
        }
    }

    private void validateIntRange(MethodVisitor mv, IntRangeValidator validator, Label failureLabel) {
        if (validator.min() > Integer.MIN_VALUE) {
            mv.visitInsn(Opcodes.DUP);
            this.loadIntConstant(mv, validator.min());
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, failureLabel);
        }
        if (validator.max() < Integer.MAX_VALUE) {
            mv.visitInsn(Opcodes.DUP);
            this.loadIntConstant(mv, validator.max());
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, failureLabel);
        }
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
            this.loadIntConstant(mv, cookie.maxAge());
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
            this.loadIntConstant(mv, cookie.version());
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

        if (this.debugMode) {
            this.concatString(mv, new Object[] {"\tResult: ", new LocalVariable(RESULT)});
            mv.visitVarInsn(Opcodes.ASTORE, this.variableIndex);
            this.loggerDebug(mv, this.variableIndex);
        }

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
                this.loadIntConstant(mv, result.type().ordinal());
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
                    this.loadIntConstant(mv, rt.ordinal());
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
        this.loadIntConstant(mv, 4096);
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

    private static class LocalVariable {

        private int variableIndex;

        public LocalVariable(int variableIndex) {
            this.variableIndex = variableIndex;
        }

        public int getVariableIndex() {
            return this.variableIndex;
        }
    }

    private void addFieldError(MethodVisitor mv, String name, String messageKey, Object... arguments) {
        // this.addFieldError("name", "messageKey");
        mv.visitVarInsn(Opcodes.ALOAD, THIS);
        mv.visitLdcInsn(name);
        mv.visitLdcInsn(messageKey);
        this.loadIntConstant(mv, arguments.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
        for (int i = 0; i < arguments.length; ++i) {
            mv.visitInsn(Opcodes.DUP);
            this.loadIntConstant(mv, i);
            if (arguments[i] instanceof LocalVariable) {
                mv.visitVarInsn(Opcodes.ALOAD, ((LocalVariable) arguments[i]).getVariableIndex());
            } else if (arguments[i] instanceof String) {
                mv.visitLdcInsn(arguments[i]);
            } else if (arguments[i] instanceof Integer) {
                this.loadIntConstant(mv, ((Integer) arguments[i]).intValue());
                MethodInvocationUtil.invokeStatic(mv, Integer.class, "toString", int.class);
            } else if (arguments[i] instanceof Long) {
                this.loadLongConstant(mv, ((Long) arguments[i]).longValue());
                MethodInvocationUtil.invokeStatic(mv, Long.class, "toString", long.class);
            } else if (arguments[i] instanceof Double) {
                this.loadDoubleConstant(mv, ((Double) arguments[i]).doubleValue());
                MethodInvocationUtil.invokeStatic(mv, Double.class, "toString", double.class);
            }
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
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    mv.visitIntInsn(Opcodes.BIPUSH, value);
                } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                    mv.visitIntInsn(Opcodes.SIPUSH, value);
                } else {
                    mv.visitLdcInsn(value);
                }
        }
    }

    private void loadLongConstant(MethodVisitor mv, long value) {
        if (value == 0) {
            mv.visitInsn(Opcodes.LCONST_0);
        } else if (value == 1) {
            mv.visitInsn(Opcodes.LCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    private void loadDoubleConstant(MethodVisitor mv, double value) {
        if (value == 0) {
            mv.visitInsn(Opcodes.DCONST_0);
        } else if (value == 1) {
            mv.visitInsn(Opcodes.DCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    private void concatString(MethodVisitor mv, Object[] strings) {
        // t = new StringBuilder("...").append(value).toString()
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(StringBuilder.class));
        mv.visitInsn(Opcodes.DUP);
        for (int i = 0; i < strings.length; ++i) {
            if (strings[i] instanceof LocalVariable) {
                mv.visitVarInsn(Opcodes.ALOAD, ((LocalVariable) strings[i]).getVariableIndex());
            } else {
                mv.visitLdcInsn(strings[i]);
            }
            if (i == 0) {
                MethodInvocationUtil.invokeConstructor(mv, StringBuilder.class, String.class);
            } else {
                MethodInvocationUtil.invokeVirtual(mv, StringBuilder.class, "append", Object.class);
            }
        }
        MethodInvocationUtil.invokeVirtual(mv, StringBuilder.class, "toString");
    }

    private static Class<?>[] supportedPrimitiveTypes =
            new Class<?>[] {boolean.class, int.class, long.class, double.class};

    private static Class<?>[] supportedWrapperTypes =
            new Class<?>[] {Boolean.class, Integer.class, Long.class, Double.class};

    public static Class<?> primitiveToWrapper(Class<?> primitiveClass) {
        for (int i = 0; i < supportedPrimitiveTypes.length; ++i) {
            if (supportedPrimitiveTypes[i].equals(primitiveClass)) {
                return supportedWrapperTypes[i];
            }
        }
        return null;
    }

    public static Class<?> wrapperToPrimitive(Class<?> wrapperClass) {
        for (int i = 0; i < supportedWrapperTypes.length; ++i) {
            if (supportedWrapperTypes[i].equals(wrapperClass)) {
                return supportedPrimitiveTypes[i];
            }
        }
        return null;
    }
}
