package com.husttwj.drawablepreview.start


import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.husttwj.drawablepreview.util.FileUtils


/**
 * 使用 StartupActivity 或 ProjectActivity， 在应用启动时执行初始化逻辑
 */
class PreviewStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        FileUtils.init()
    }

}

