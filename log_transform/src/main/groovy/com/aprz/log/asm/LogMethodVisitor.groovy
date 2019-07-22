package com.aprz.log.asm


import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author lyl* @Date 2019/7/21
 * This class is used for:
 */
class LogMethodVisitor extends MethodVisitor {

    String name

    LogMethodVisitor(MethodVisitor mv, String name) {
        super(Opcodes.ASM6, mv)
        this.name = name
    }


    /**
     *    L2
     *     LINENUMBER 13 L2
     *     LDC "log_inject"
     *     LDC "onCreate"
     *     INVOKESTATIC android/util/Log.e (Ljava/lang/String;Ljava/lang/String;)I
     *     POP
     */
    @Override
    void visitCode() {
        super.visitCode()
        // 在方法之前插入 Log.e("", "")
        // 这两个是参数
        this.mv.visitLdcInsn('log_inject')
        this.mv.visitLdcInsn(this.name)
        this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, 'android/util/Log', 'e', '(Ljava/lang/String;Ljava/lang/String;)I', false)
        // 这里的用法有点奇怪，还需要研究一下
        // visitXXX 实际上会触发 MethodWriter 的方法，这些方法会将我们想要写入的字节码存放起来
        // 最后统一的写入到输出的 class 文件中
    }

    /**
     *     MAXSTACK = 2
     *     MAXLOCALS = 2
     * @param maxStack
     * @param maxLocals
     */
    @Override
    void visitMaxs(int maxStack, int maxLocals) {
        // 修改后方法需要的栈帧 可以从 byteCode 里面看到
        super.visitMaxs(2, 2)
    }
}
