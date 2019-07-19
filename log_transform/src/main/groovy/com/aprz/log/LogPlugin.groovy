package com.aprz.log

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class LogPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // 找到项目中的 某个继承至 BaseExtension 的扩展
        def ext = project.extensions.getByType(BaseExtension)
        // 往该扩展中添加 transform
        // 这里其实就是将我们自定义的这个 transform 添加到了集合中
        // 但是这里让我想不明白的是，为什么这个添加 transform 的方法是在 extension 里面
        // 而不是 project 里面，如果像 Java 工程，没有使用有扩展的插件该怎么办

        // 查看源码发现了这样的代码：com.android.build.gradle.internal.TaskManager.createPostCompilationTasks
        // AndroidConfig extension = variantScope.getGlobalScope().getExtension();
        // 它获取到了 android 扩展，然后拿到了其中的所有 transform
        // 嗯，看来这个是针对 Android 构建的
        ext.registerTransform(new LogsTransform(project))
    }

}