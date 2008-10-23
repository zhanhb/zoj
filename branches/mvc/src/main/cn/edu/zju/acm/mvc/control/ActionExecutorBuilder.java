
package cn.edu.zju.acm.mvc.control;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import cn.edu.zju.acm.onlinejudge.util.Pair;

public class ActionExecutorBuilder {

    private static final int THIS = 0;

    private static final int REQ = 1;

    private static final int RESP = 2;

    private static final int FORWARD = 3;

    private static final int VALUE = 4;

    private static final int LENGTH = 5;

    private static final int I = 6;

    private static final int SESSION = 7;

    private static final int VAR1 = 8;

    private static final int VAR2 = 9;

    private static final int VAR3 = 10;

    private static ActionExecutorClassLoader actionExecutorClassLoader = new ActionExecutorClassLoader();

    private Logger logger = Logger.getLogger(ActionExecutorBuilder.class);

    private boolean debugMode;

    private ClassWriter cw;

    private String parentInternalName;

    private String internalName;

    private Class<?> superClass;

    private ActionDescriptor actionDescriptor;

    public Class<? extends ActionExecutor> build(ActionDescriptor actionDescriptor, boolean debugMode) {
        this.actionDescriptor = actionDescriptor;
        this.superClass = actionDescriptor.getActionClass();
        this.debugMode = debugMode;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        this.parentInternalName = Type.getInternalName(this.superClass);
        this.internalName = this.parentInternalName + "Executor";
        this.cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, this.internalName, null,
                      this.parentInternalName, new String[] {Type.getInternalName(ActionExecutor.class)});
        if (debugMode) {
            FieldVisitor fv =
                    this.cw.visitField(Opcodes.ACC_PRIVATE, "logger", Type.getDescriptor(Logger.class), null, null);
            fv.visitEnd();
        }
        this.buildConstructor(this.parentInternalName, this.internalName, debugMode);
        this.buildExecute();
        this.cw.visitEnd();
        return ActionExecutorBuilder.actionExecutorClassLoader.defineClass(this.superClass.getName() + "Executor",
                                                                           this.cw.toByteArray());
    }

    private void buildConstructor(String parentInternalName, String internalName, boolean debugMode) {
        MethodVisitor mv = this.cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();

        // super()
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, parentInternalName, "<init>", "()V");

        if (debugMode) {
            // logger = Logger.getLogger("XXXExecutor")
            mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
            mv.visitLdcInsn(this.superClass.getName() + "Executor");
            this.invokeStatic(mv, Logger.class, "getLogger", String.class);
            mv.visitFieldInsn(Opcodes.PUTFIELD, internalName, "logger", Type.getDescriptor(Logger.class));
        }

        mv.visitInsn(Opcodes.RETURN);
        mv.visitEnd();
    }

    private void buildExecute() {
        MethodVisitor mv =
                this.cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", this.getMethodDescriptor(ActionExecutor.class,
                                                                                            "execute",
                                                                                            HttpServletRequest.class,
                                                                                            HttpServletResponse.class,
                                                                                            boolean.class), null,
                                    new String[] {Type.getInternalName(Exception.class)});
        mv.visitCode();
        if (this.debugMode) {
            mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
            mv.visitFieldInsn(Opcodes.GETFIELD, ActionExecutorBuilder.this.internalName, "logger",
                              Type.getDescriptor(Logger.class));
            mv.visitLdcInsn("Enter " + this.superClass.getName());
            ActionExecutorBuilder.this.invokeVirtual(mv, Logger.class, "debug", Object.class);
        }

        if (this.actionDescriptor.isNeedCookies()) {
            this.buildInitializeCookieMap(mv);
        }

        for (PropertyDescriptor propertyDescriptor : this.actionDescriptor.getPropertyList()) {
            if (propertyDescriptor.isGetFromSession() || propertyDescriptor.isSetToSession()) {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                this.invokeInterface(mv, HttpServletRequest.class, "getSession");
                mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.SESSION);
            }
        }

        Label notForwardLabel = new Label();
        Label executionLabel = new Label();
        mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.FORWARD);
        mv.visitJumpInsn(Opcodes.IFEQ, notForwardLabel);
        this.buildInitialization(mv, true);
        mv.visitJumpInsn(Opcodes.GOTO, executionLabel);
        mv.visitLabel(notForwardLabel);
        this.buildInitialization(mv, false);
        mv.visitLabel(executionLabel);
        this.buildExecution(mv);
        mv.visitEnd();
    }

    private void buildInitializeCookieMap(MethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(HashMap.class));
        mv.visitInsn(Opcodes.DUP);
        this.invokeConstructor(mv, HashMap.class);
        mv.visitInsn(Opcodes.DUP);
        mv.visitFieldInsn(Opcodes.PUTFIELD, this.internalName, "cookieMap", Type.getDescriptor(Map.class));
        mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR1);
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.REQ);
        this.invokeInterface(mv, HttpServletRequest.class, "getCookies");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR2);
        mv.visitInsn(Opcodes.ARRAYLENGTH);
        mv.visitVarInsn(Opcodes.ISTORE, ActionExecutorBuilder.LENGTH);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, ActionExecutorBuilder.I);
        Label forTestLabel = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, forTestLabel);
        Label forBodyLabel = new Label();
        mv.visitLabel(forBodyLabel);
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR2);
        mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.I);
        mv.visitInsn(Opcodes.AALOAD);
        mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR3);
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR1);
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR3);
        this.invokeVirtual(mv, Cookie.class, "getName");
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR3);
        this.invokeInterface(mv, Map.class, "put", Object.class, Object.class);
        mv.visitInsn(Opcodes.POP);
        mv.visitIincInsn(ActionExecutorBuilder.I, 1);
        mv.visitLabel(forTestLabel);
        mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.I);
        mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.LENGTH);
        mv.visitJumpInsn(Opcodes.IF_ICMPLT, forBodyLabel);
    }

    private void buildInitialization(MethodVisitor mv, boolean forward) {
        for (PropertyDescriptor prop : this.actionDescriptor.getPropertyList()) {
            if ((prop.canGetFromParameter() || forward) && prop.getSetter() != null) {
                this.new PropertyInitializationBuilder(mv, prop, forward).build();
            }
        }
    }

    private void buildFillAttributes(MethodVisitor mv) {
        for (PropertyDescriptor prop : this.actionDescriptor.getPropertyList()) {
            if (prop.getGetter() != null) {
                mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.REQ);
                mv.visitLdcInsn(prop.getName());
                mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
                this.invokeSelfVirtual(mv, prop.getGetter().getName());
                if (prop.getElementClass().isPrimitive()) {
                    java.lang.reflect.Type targetType = ActionExecutorBuilder.primitiveToWrapper(prop.getElementClass());
                    this.invokeStatic(mv, (Class<?>) targetType, "valueOf", prop.getElementClass());
                }
                if (prop.isSetToSession()) {
                    mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR3);
                    mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR3);
                }
                ActionExecutorBuilder.this.invokeInterface(mv, HttpServletRequest.class, "setAttribute", String.class,
                                                           Object.class);
                if (prop.isSetToSession()) {
                    mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.SESSION);
                    mv.visitLdcInsn(prop.getName());
                    mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR3);
                    ActionExecutorBuilder.this.invokeInterface(mv, HttpSession.class, "setAttribute", String.class,
                                                               Object.class);
                }
            }
        }
    }

    private void buildAddCookies(MethodVisitor mv, Label endLabel) {
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
        mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "outputCookies", Type.getDescriptor(List.class));
        mv.visitJumpInsn(Opcodes.IFNULL, endLabel);

        // i = this.outputCookies.iterator()
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
        mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "outputCookies", Type.getDescriptor(List.class));
        this.invokeInterface(mv, List.class, "iterator");
        mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.I);

        // goto for_test
        Label forTestLabel = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, forTestLabel);

        // for_body:
        Label forBodyLabel = new Label();
        mv.visitLabel(forBodyLabel);

        // resp.addCookie((Cookie)i.next())
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.RESP);
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.I);
        this.invokeInterface(mv, Iterator.class, "next");
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Cookie.class));
        this.invokeInterface(mv, HttpServletResponse.class, "addCookie", Cookie.class);
        mv.visitLabel(forTestLabel);
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.I);
        this.invokeInterface(mv, Iterator.class, "hasNext");
        mv.visitJumpInsn(Opcodes.IFNE, forBodyLabel);
    }

    private void buildExecution(MethodVisitor mv) {
        List<Pair<String, String>> resultList = this.actionDescriptor.getResultMap();
        List<Pair<Class<? extends Throwable>, String>> exceptionList = this.actionDescriptor.getExceptionMap();

        // var1 = null
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR1);

        Label startTryCatchLabel = new Label();
        Label endTryCatchLabel = new Label();
        List<Label> labelList = new ArrayList<Label>();
        for (int i = 0; i < exceptionList.size(); ++i) {
            Label label = new Label();
            labelList.add(label);
            mv.visitTryCatchBlock(startTryCatchLabel, labelList.get(0), label,
                                  Type.getInternalName(exceptionList.get(i).getFirst()));
        }

        // start_try_catch:
        mv.visitLabel(startTryCatchLabel);

        // var2 = super.execute()
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.parentInternalName, "execute",
                           this.getMethodDescriptor(this.superClass, "execute"));
        mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR2);

        for (Pair<String, String> pair : resultList) {
            // if (!"xxx".equals(var2)) goto not_equal
            mv.visitLdcInsn(pair.getFirst());
            mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR2);
            this.invokeVirtual(mv, String.class, "equals", Object.class);
            Label notEqualsLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, notEqualsLabel);

            // VAR1 = "yyy";
            mv.visitLdcInsn(pair.getSecond());
            mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR1);

            // goto end_try_catch
            mv.visitJumpInsn(Opcodes.GOTO, endTryCatchLabel);

            // not_equal
            mv.visitLabel(notEqualsLabel);
        }

        // if (result != null) goto result_not_null
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR1);
        Label resultNotNullLabel = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, resultNotNullLabel);

        // throw new InvalidResultException(var2)
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(InvalidResultException.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR2);
        this.invokeConstructor(mv, InvalidResultException.class, String.class);
        mv.visitInsn(Opcodes.ATHROW);

        // result_not_null:
        mv.visitLabel(resultNotNullLabel);

        this.buildFillAttributes(mv);

        this.buildAddCookies(mv, endTryCatchLabel);

        for (int i = 0; i < exceptionList.size(); ++i) {
            // goto end_try_catch
            mv.visitJumpInsn(Opcodes.GOTO, endTryCatchLabel);

            // catch(XXXException e)
            mv.visitLabel(labelList.get(i));
            mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.I);

            // result = "zzz";
            mv.visitLdcInsn(exceptionList.get(i).getSecond());
            mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR1);
        }

        // end_try_catch:
        mv.visitLabel(endTryCatchLabel);

        // return result
        mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR1);
        mv.visitInsn(Opcodes.ARETURN);
    }

    private class PropertyInitializationBuilder {

        private Class<?> containerClass = null;

        private Class<?> elementClass = null;

        private MethodVisitor mv;

        private String propertyName;

        private Class<? extends Exception> possibleExceptionClass;

        private PropertyDescriptor propertyDescriptor;

        private boolean forward;

        private Label endLabel = new Label();

        public PropertyInitializationBuilder(MethodVisitor mv, PropertyDescriptor propertyDescriptor, boolean forward) {
            this.propertyDescriptor = propertyDescriptor;
            this.mv = mv;
            this.containerClass = propertyDescriptor.getContainerClass();
            this.elementClass = propertyDescriptor.getElementClass();
            this.propertyName = propertyDescriptor.getName();
            this.possibleExceptionClass = propertyDescriptor.getPossibleExceptionClass();
            this.forward = forward;
        }

        public void build() {
            StringBuilder builder = new StringBuilder(ActionExecutorBuilder.this.superClass.getName());
            builder.append('.');
            builder.append(this.propertyName);
            builder.append(' ');
            if (this.containerClass == null) {
                builder.append(this.elementClass.getName());
            } else if (this.containerClass.isArray()) {
                builder.append(this.elementClass.getName());
                builder.append("[]");
            } else {
                builder.append(this.containerClass.getName());
                builder.append('<');
                builder.append(this.elementClass.getName());
                builder.append('>');
            }
            ActionExecutorBuilder.this.logger.debug(builder);
            Label startTryCatchLabel = new Label();
            Label possibleExceptionLabel = new Label();
            Label classCastExceptionLabel = this.possibleExceptionClass == null ? possibleExceptionLabel : new Label();
            boolean hasClassCastException = this.forward || this.propertyDescriptor.isGetFromSession();
            boolean hasException = this.possibleExceptionClass != null || hasClassCastException;
            if (hasException) {
                if (this.possibleExceptionClass != null) {
                    this.mv.visitTryCatchBlock(startTryCatchLabel, possibleExceptionLabel, possibleExceptionLabel,
                                               Type.getInternalName(this.possibleExceptionClass));
                }
                if (hasClassCastException) {
                    this.mv.visitTryCatchBlock(startTryCatchLabel, possibleExceptionLabel, classCastExceptionLabel,
                                               Type.getInternalName(ClassCastException.class));
                }
                this.mv.visitLabel(startTryCatchLabel);
            }
            this.buildGetFromRequestAttributes();
            this.buildGetFromParameters();
            this.buildGetFromSessionAttributes();
            if (hasException) {
                if (this.possibleExceptionClass != null) {
                    this.mv.visitLabel(possibleExceptionLabel);
                    this.buildExceptionHandler();
                    if (hasClassCastException) {
                        this.mv.visitJumpInsn(Opcodes.GOTO, this.endLabel);
                    }
                }
                if (hasClassCastException) {
                    this.mv.visitLabel(classCastExceptionLabel);
                    this.buildExceptionHandler();
                }
            }
            this.mv.visitLabel(this.endLabel);
        }

        private void buildExceptionHandler() {
            // save e
            this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.I);

            if (ActionExecutorBuilder.this.debugMode) {
                // logger.debug("Fail to set property XXX", e)
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
                this.mv.visitFieldInsn(Opcodes.GETFIELD, ActionExecutorBuilder.this.internalName, "logger",
                                       Type.getDescriptor(Logger.class));
                this.mv.visitLdcInsn("Fail to set property " + this.propertyName);
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.I);
                ActionExecutorBuilder.this.invokeVirtual(this.mv, Logger.class, "debug", Object.class, Throwable.class);
            }

            // this.addErrorMessage("XXX should be of type XXX")
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
            this.mv.visitLdcInsn(this.propertyName + " should be of type " + this.elementClass.getCanonicalName());
            ActionExecutorBuilder.this.invokeSelfVirtual(this.mv, "addErrorMessage", String.class);
        }

        private void buildGetFromRequestAttributes() {
            if (!this.forward) {
                return;
            }

            // value = req.getAttribute(propertyName)
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.REQ);
            this.mv.visitLdcInsn(this.propertyName);
            ActionExecutorBuilder.this.invokeInterface(this.mv, HttpServletRequest.class, "getAttribute", String.class);
            this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VALUE);

            // if (value == null) goto get_from_parameter
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VALUE);
            Label getFromParametersLabel = new Label();
            this.mv.visitJumpInsn(Opcodes.IFNULL, getFromParametersLabel);

            this.buildSetValue(false);
            this.mv.visitJumpInsn(Opcodes.GOTO, this.endLabel);

            // get_from_parameter:
            this.mv.visitLabel(getFromParametersLabel);
        }

        private void buildGetFromParameters() {
            // value = req.getParameter(propertyName) or value = req.getParameters(propertyName)
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.REQ);
            this.mv.visitLdcInsn(this.propertyName);
            if (this.containerClass == null) {
                ActionExecutorBuilder.this.invokeInterface(this.mv, HttpServletRequest.class, "getParameter",
                                                           String.class);
            } else {
                ActionExecutorBuilder.this.invokeInterface(this.mv, HttpServletRequest.class, "getParameterValues",
                                                           String.class);
            }
            this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VALUE);

            // if (value == null) goto get_from_session_attributes
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VALUE);
            Label getFromSessionAttributesLabel = new Label();
            if (this.propertyDescriptor.isGetFromSession()) {
                this.mv.visitJumpInsn(Opcodes.IFNULL, getFromSessionAttributesLabel);
            } else {
                this.mv.visitJumpInsn(Opcodes.IFNULL, this.endLabel);
            }

            this.buildParameterValueNotNullBlock();

            if (this.propertyDescriptor.isGetFromSession()) {
                this.mv.visitJumpInsn(Opcodes.GOTO, this.endLabel);

                // get_from_session_attributes:
                this.mv.visitLabel(getFromSessionAttributesLabel);
            }
        }

        private void buildGetFromSessionAttributes() {
            if (!this.propertyDescriptor.isGetFromSession()) {
                return;
            }

            // value = session.getAttribute(propertyName)
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.SESSION);
            this.mv.visitLdcInsn(this.propertyName);
            ActionExecutorBuilder.this.invokeInterface(this.mv, HttpSession.class, "getAttribute", String.class);
            this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VALUE);

            // if (value == null) goto end
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VALUE);
            this.mv.visitJumpInsn(Opcodes.IFNULL, this.endLabel);

            this.buildSetValue(false);
        }

        private void buildSetValue(boolean convert) {
            // this.setXXX(value)
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.THIS);
            this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VALUE);
            if (convert) {
                this.convertElement();
            } else {
                java.lang.reflect.Type targetType = ActionExecutorBuilder.primitiveToWrapper(this.elementClass);
                if (targetType != this.elementClass) {
                    // Integer.intValue(), Long.longValue(), etc
                    ActionExecutorBuilder.this.invokeVirtual(this.mv, (Class<?>) targetType, this.elementClass.getName() + "Value");
                }
            }
            ActionExecutorBuilder.this.invokeSelfVirtual(this.mv, this.propertyDescriptor.getSetter().getName(),
                                                         this.propertyDescriptor.getTypeClass());
        }

        private void buildParameterValueNotNullBlock() {
            if (this.containerClass == null) {
                this.buildSetValue(true);
            } else {
                // length = values.length
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VALUE);
                this.mv.visitInsn(Opcodes.ARRAYLENGTH);
                this.mv.visitVarInsn(Opcodes.ISTORE, ActionExecutorBuilder.LENGTH);

                if (this.containerClass.isArray()) {
                    // var1 = new xxx[length]
                    this.mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.LENGTH);
                    if (this.elementClass.isPrimitive()) {
                        this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
                        if (this.elementClass.equals(byte.class)) {
                            this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
                        } else if (this.elementClass.equals(short.class)) {
                            this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_SHORT);
                        } else if (this.elementClass.equals(int.class)) {
                            this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
                        } else if (this.elementClass.equals(long.class)) {
                            this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
                        } else if (this.elementClass.equals(float.class)) {
                            this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
                        } else if (this.elementClass.equals(double.class)) {
                            this.mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
                        }
                    } else {
                        this.mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(this.elementClass));
                    }
                    this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR1);
                } else if (List.class.equals(this.containerClass)) {
                    // var1 = new ArrayList(length)
                    this.mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(ArrayList.class));
                    this.mv.visitInsn(Opcodes.DUP);
                    this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.LENGTH);
                    ActionExecutorBuilder.this.invokeConstructor(this.mv, ArrayList.class, int.class);
                    this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR1);
                } else if (Set.class.equals(this.containerClass)) {
                    // var1 = new HashSet()
                    this.mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(HashSet.class));
                    this.mv.visitInsn(Opcodes.DUP);
                    ActionExecutorBuilder.this.invokeConstructor(this.mv, HashSet.class);
                    this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR1);
                } else {
                    // Should not reach here
                    throw new RuntimeException("Invalid containerClass " + this.containerClass.getName());
                }

                // i = 0
                this.mv.visitInsn(Opcodes.ICONST_0);
                this.mv.visitVarInsn(Opcodes.ISTORE, ActionExecutorBuilder.I);

                // goto for_test
                Label forTestLabel = new Label();
                this.mv.visitJumpInsn(Opcodes.GOTO, forTestLabel);

                // for_body:
                Label forBodyLabel = new Label();
                this.mv.visitLabel(forBodyLabel);

                // var1 or var1[i]
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR1);
                if (this.containerClass.isArray()) {
                    this.mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.I);
                }

                // convert(value[i])
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VALUE);
                this.mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.I);
                this.mv.visitInsn(Opcodes.AALOAD);
                this.convertElement();

                if (this.containerClass.isArray()) {
                    // var1[i] = top_of_stack
                    if (this.elementClass.equals(byte.class)) {
                        this.mv.visitInsn(Opcodes.BASTORE);
                    } else if (this.elementClass.equals(short.class)) {
                        this.mv.visitInsn(Opcodes.SASTORE);
                    } else if (this.elementClass.equals(int.class)) {
                        this.mv.visitInsn(Opcodes.IASTORE);
                    } else if (this.elementClass.equals(long.class)) {
                        this.mv.visitInsn(Opcodes.LASTORE);
                    } else if (this.elementClass.equals(float.class)) {
                        this.mv.visitInsn(Opcodes.FASTORE);
                    } else if (this.elementClass.equals(double.class)) {
                        this.mv.visitInsn(Opcodes.DASTORE);
                    } else {
                        this.mv.visitInsn(Opcodes.AASTORE);
                    }
                } else {
                    // var1.add(top_of_stack)
                    ActionExecutorBuilder.this.invokeInterface(this.mv, this.containerClass, "add", Object.class);
                    this.mv.visitInsn(Opcodes.POP);
                }

                // i++
                this.mv.visitIincInsn(ActionExecutorBuilder.I, 1);

                // for_test:
                this.mv.visitLabel(forTestLabel);

                // if (i < length) goto for_body
                this.mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.I);
                this.mv.visitVarInsn(Opcodes.ILOAD, ActionExecutorBuilder.LENGTH);
                this.mv.visitJumpInsn(Opcodes.IF_ICMPLT, forBodyLabel);

                // value = var1
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR1);
                this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VALUE);
            }
        }

        private void convertElement() {
            if (this.elementClass.isPrimitive()) {
                // Integer.parseInt(value), Long.parseLong(value), etc
                java.lang.reflect.Type wrapperClass = ActionExecutorBuilder.primitiveToWrapper(this.elementClass);
                ActionExecutorBuilder.this.invokeStatic(this.mv, (Class<?>) wrapperClass, "parse" +
                    ActionUtil.capitalize(this.elementClass.getName()), String.class);
            } else if (this.elementClass.equals(String.class)) {
                // do nothing
            } else if (this.elementClass.equals(Date.class)) {
                this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR2);
                // fmt = new SimpleDateFormat()
                this.mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(SimpleDateFormat.class));
                this.mv.visitInsn(Opcodes.DUP);
                ActionExecutorBuilder.this.invokeConstructor(this.mv, SimpleDateFormat.class);
                this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR3);

                // fmt.setLenient(false)
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR3);
                this.mv.visitInsn(Opcodes.ICONST_0);
                ActionExecutorBuilder.this.invokeVirtual(this.mv, SimpleDateFormat.class, "setLenient", boolean.class);

                // fmt.parse(value)
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR3);
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR2);
                ActionExecutorBuilder.this.invokeVirtual(this.mv, SimpleDateFormat.class, "parse", String.class);
            } else if (this.elementClass.equals(File.class)) {
            } else {
                // new Integer(value), new BigInteger(value), etc
                this.mv.visitVarInsn(Opcodes.ASTORE, ActionExecutorBuilder.VAR2);
                this.mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(this.elementClass));
                this.mv.visitInsn(Opcodes.DUP);
                this.mv.visitVarInsn(Opcodes.ALOAD, ActionExecutorBuilder.VAR2);
                ActionExecutorBuilder.this.invokeConstructor(this.mv, this.elementClass, String.class);
            }
        }
    }

    private String getConstructorDescriptor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return Type.getConstructorDescriptor(clazz.getConstructor(parameterTypes));
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            return null;
        }
    }

    private String getMethodDescriptor(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return Type.getMethodDescriptor(clazz.getMethod(methodName, parameterTypes));
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            return null;
        }
    }

    private void invokeSelfMethod(MethodVisitor mv, int opcode, String methodName, Class<?>... parameterTypes) {
        mv.visitMethodInsn(opcode, this.internalName, methodName, this.getMethodDescriptor(this.superClass, methodName,
                                                                                           parameterTypes));
    }

    private void invokeSelfVirtual(MethodVisitor mv, String methodName, Class<?>... parameterTypes) {
        this.invokeSelfMethod(mv, Opcodes.INVOKEVIRTUAL, methodName, parameterTypes);
    }

    private void invokeMethod(MethodVisitor mv, int opcode, Class<?> clazz, String methodName,
                              Class<?>... parameterTypes) {
        mv.visitMethodInsn(opcode, Type.getInternalName(clazz), methodName, this.getMethodDescriptor(clazz, methodName,
                                                                                                     parameterTypes));
    }

    private void invokeStatic(MethodVisitor mv, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        this.invokeMethod(mv, Opcodes.INVOKESTATIC, clazz, methodName, parameterTypes);
    }

    private void invokeConstructor(MethodVisitor mv, Class<?> clazz, Class<?>... parameterTypes) {
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(clazz), "<Init>",
                           this.getConstructorDescriptor(clazz, parameterTypes));
    }

    private void invokeVirtual(MethodVisitor mv, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        this.invokeMethod(mv, Opcodes.INVOKEVIRTUAL, clazz, methodName, parameterTypes);
    }

    private void invokeInterface(MethodVisitor mv, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        this.invokeMethod(mv, Opcodes.INVOKEINTERFACE, clazz, methodName, parameterTypes);

    }

    private static java.lang.reflect.Type primitiveToWrapper(java.lang.reflect.Type type) {
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
        return type;
    }
}
