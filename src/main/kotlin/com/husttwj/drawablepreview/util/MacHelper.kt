package com.husttwj.drawablepreview.util


import java.io.File
import java.nio.charset.Charset

class MacHelper : OSHelper() {

    private var sPluginShellPath: String? = null

    override fun init() {
        sPluginShellPath =
            FileUtils.sPluginDir.replace(" ", "\\ ")
        if (!File(FileUtils.sPluginDir, "imgcopy").exists()) {
            try {
                execCommand("gcc -Wall -g -O3 -ObjC -framework Foundation -framework AppKit -o $sPluginShellPath/imgcopy $sPluginShellPath/imgcopy.m")
            } catch (ignore: Exception) {
            }
        }
        try {
            execCommand("chmod a+x $sPluginShellPath/imgcopy")
        } catch (e: Exception) {
            LogUtil.e("组件初始化失败 Path: $sPluginShellPath", e)
        }
    }


    @Throws(java.lang.Exception::class)
    override fun execCommand(vararg command: String): ExecResult {
        return execCommand(false, *command)
    }

    @Throws(java.lang.Exception::class)
    fun execCommand(noHup: Boolean, vararg commands: String): ExecResult {
        for (i in commands.indices) {
            var zshCommands = if (noHup) {
                arrayOf("/bin/zsh", "-c", commands[i])
            } else {
                arrayOf("nohup", "/bin/zsh", "-c", commands[i])
            }
            val exec = Runtime.getRuntime().exec(zshCommands)
            var byteArrayOutputStream = FileUtils.readByteArrayOutputStream(exec.inputStream)
            exec.waitFor()
            val resuleCode = exec.exitValue()
            val errorResultStream = FileUtils.readByteArrayOutputStream(exec.errorStream)
            if (byteArrayOutputStream.size() == 0 || (resuleCode != 0 && errorResultStream.size() > 0)) {
                byteArrayOutputStream = errorResultStream;
            }
            if (i == commands.size - 1) {
                return ExecResult(
                    resuleCode,
                    if (resuleCode == 0) String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8")) else null,
                    if (resuleCode != 0) String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8")) else null
                )
            }
        }
        return ExecResult(0, null, null)
    }

}