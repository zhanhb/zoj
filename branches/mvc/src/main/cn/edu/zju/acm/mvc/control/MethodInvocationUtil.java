
package cn.edu.zju.acm.mvc.control;

import org.apache.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodInvocationUtil {

    private static Logger logger = Logger.getLogger(MethodInvocationUtil.class);

    public static String getConstructorDescriptor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return Type.getConstructorDescriptor(clazz.getConstructor(parameterTypes));
        } catch (Exception e) {
            MethodInvocationUtil.logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getMethodDescriptor(Class<?> returnType, Class<?>... parameterTypes) {
        Type[] params = new Type[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            params[i] = Type.getType(parameterTypes[i]);
        }
        return Type.getMethodDescriptor(Type.getType(returnType), params);
    }

    public static String getMethodDescriptor(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return Type.getMethodDescriptor(clazz.getMethod(methodName, parameterTypes));
        } catch (Exception e) {
            MethodInvocationUtil.logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static void invokeConstructor(MethodVisitor mv, Class<?> clazz, Class<?>... parameterTypes) {
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(clazz), "<Init>",
                           MethodInvocationUtil.getConstructorDescriptor(clazz, parameterTypes));
    }

    public static void invokeInterface(MethodVisitor mv, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        MethodInvocationUtil.invokeMethod(mv, Opcodes.INVOKEINTERFACE, clazz, methodName, parameterTypes);

    }

    public static void invokeInterface(MethodVisitor mv, String internalName, Class<?> returnType, String methodName,
                                       Class<?>... parameterTypes) {
        MethodInvocationUtil.invokeMethod(mv, Opcodes.INVOKEINTERFACE, internalName, returnType, methodName, parameterTypes);
    }

    private static void invokeMethod(MethodVisitor mv, int opcode, Class<?> clazz, String methodName,
                                     Class<?>... parameterTypes) {
        mv.visitMethodInsn(opcode, Type.getInternalName(clazz), methodName,
                           MethodInvocationUtil.getMethodDescriptor(clazz, methodName, parameterTypes));
    }

    private static void invokeMethod(MethodVisitor mv, int opcode, String internalName, Class<?> returnType,
                                     String methodName, Class<?>... parameterTypes) {

        mv.visitMethodInsn(opcode, internalName, methodName,
                           MethodInvocationUtil.getMethodDescriptor(returnType, parameterTypes));
    }

    public static void invokeStatic(MethodVisitor mv, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        MethodInvocationUtil.invokeMethod(mv, Opcodes.INVOKESTATIC, clazz, methodName, parameterTypes);
    }

    public static void invokeStatic(MethodVisitor mv, String internalName, Class<?> returnType, String methodName,
                                    Class<?>... parameterTypes) {
        MethodInvocationUtil.invokeMethod(mv, Opcodes.INVOKESTATIC, internalName, returnType, methodName, parameterTypes);
    }

    public static void invokeVirtual(MethodVisitor mv, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        MethodInvocationUtil.invokeMethod(mv, Opcodes.INVOKEVIRTUAL, clazz, methodName, parameterTypes);
    }

    public static void invokeVirtual(MethodVisitor mv, String internalName, Class<?> returnType, String methodName,
                                     Class<?>... parameterTypes) {
        MethodInvocationUtil.invokeMethod(mv, Opcodes.INVOKEVIRTUAL, internalName, returnType, methodName, parameterTypes);
    }
}
