package com.aprz.log.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author lyl* @Date 2019/7/21
 * This class is used for:
 */
class LogClassVisitor extends ClassVisitor {

    private String superName
    private String[] interfaces

    LogClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM6, cv)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.superName = superName
        // 记录该类实现了哪些接口
        this.interfaces = interfaces
        super.visit(version, access, name, signature, superName, interfaces)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!checkSuperClass(this.superName)) {
            return super.visitMethod(access, name, desc, signature, exceptions)
        }

        // 由于是一个例子，我们就只处理 onCreate 方法了，想要深入应该去研究一下一个正规的开源项目

        // 我的 demo 是 kotlin，tools里面有工具可以直接查看字节码，就非常方便
        if ('onCreate(Landroid/os/Bundle;)V' == (name + desc)) {

            println "log >>> method name = ${name + desc}"

            MethodVisitor methodVisitor = this.cv.visitMethod(access, name, desc, signature, exceptions)
            return new LogMethodVisitor(methodVisitor, name)
        }

        return super.visitMethod(access, name, desc, signature, exceptions)
    }

    /**
     * 一般情况下，这里应该使用注解
     * @param superName
     * @return
     */
    static boolean checkSuperClass(String superName) {
        println "log inject >>> superName = ${superName}"
        return superName == 'androidx/appcompat/app/AppCompatActivity'
    }

}
