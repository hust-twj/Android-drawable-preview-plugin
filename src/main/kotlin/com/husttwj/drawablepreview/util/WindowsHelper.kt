package com.husttwj.drawablepreview.util


import java.io.File
import java.nio.charset.Charset

class WindowsHelper : OSHelper() {

    override fun init() {
    }


    @Throws(java.lang.Exception::class)
    override fun execCommand(vararg command: String): ExecResult {
        return execCommand(false, false, *command)
    }

    @Throws(java.lang.Exception::class)
    fun execCommand(nohub: Boolean, powerShell: Boolean, vararg commands: String): ExecResult {
        for (i in commands.indices) {
            var shellCmds = if (powerShell) {
                arrayOf("cmd", "/C", "powershell", commands[i].trim())
            } else if (nohub) {
                arrayOf("cmd", "/C", "start /min \"n\" ${commands[i].trim()}")
            } else {
                arrayOf("cmd", "/C", commands[i].trim())
            }
            val exec = Runtime.getRuntime().exec(shellCmds)
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
                    if (resuleCode == 0) String(byteArrayOutputStream.toByteArray(), Charset.forName("GB2312")) else null,
                    if (resuleCode != 0) String(byteArrayOutputStream.toByteArray(), Charset.forName("GB2312")) else null
                )
            }
        }
        return ExecResult(0, null, null)
    }

}