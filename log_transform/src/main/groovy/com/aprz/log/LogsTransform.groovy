package com.aprz.log

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.aprz.log.asm.LogClassVisitor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class LogsTransform extends Transform {

    Project project

    LogsTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return this.getClass().getSimpleName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        // inputs 包含了 jar 包和目录。
        // 子 module 的 java 文件在编译过程中也会生成一个 jar 包然后编译到主工程中。
        transformInvocation.inputs.each {
            input ->

                // 遍历目录
                // 文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                input.directoryInputs.each {
                    DirectoryInput directoryInput ->
                        directoryInput.file.eachFileRecurse {
                            File file ->
                                if (checkFileName(file.name)) {
                                    injectClassFile(file)
                                }
                        }
                        copyDirectory(directoryInput, transformInvocation.outputProvider)
                }


                // 遍历 jar，我们不需要对 jar 进行处理，所以直接跳过
                // 但是后面的 transform 可能需要处理，所以需要从输入流原封不动的写到输出流
                input.jarInputs.each {
                    jarInput ->
                        copyJar(jarInput, transformInvocation.outputProvider)
                }
        }
    }

    static boolean checkFileName(String name) {
        return name.endsWith(".class") && !name.startsWith("R\$") &&
                "R.class" != name && "BuildConfig.class" != name
    }

    static void injectClassFile(File file) {
        ClassReader classReader = new ClassReader(file.bytes)
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        ClassVisitor cv = new LogClassVisitor(classWriter)
        classReader.accept(cv, ClassReader.EXPAND_FRAMES)
        byte[] code = classWriter.toByteArray()
        FileOutputStream fos = new FileOutputStream(
                file.parentFile.absolutePath + File.separator + file.name)
        fos.write(code)
        fos.close()
    }

    static void copyDirectory(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        // 获取output目录
        def dest = outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

        // 将input的目录复制到output指定目录
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    static void copyJar(JarInput jarInput, TransformOutputProvider outputProvider) {
        // 重命名输出文件（同目录copyFile会冲突）
        // 这里也是我的一个疑惑的地方
        // 几乎所有网上的代码都是这样的
        // 难道说是 一个 transform 从一个目录读取 jar 文件，处理完成之后然后写回这个目录？？？
        def jarName = jarInput.name
        println("jar = " + jarInput.file.getAbsolutePath())
        def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
        // 避免出现 xxx.jar.jar 这样的名字
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4)
        }
        def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        FileUtils.copyFile(jarInput.file, dest)
    }
}