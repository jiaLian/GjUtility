package com.goodjia.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.view.View
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader

const val FULL_SCREEN_UI_OPTIONS = (
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

fun Activity.fullscreenOnWindowFocusChanged(
    hasFocus: Boolean,
    visibility: Int = FULL_SCREEN_UI_OPTIONS
) {
    if (hasFocus) {
        window.decorView.systemUiVisibility = visibility
    }
}

fun Activity.restartAPP() {
    val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
}

fun runtimeCommand(command: String): String? {
    return try {
        val isRooted = isRooted()
        Logger.d("RuntimeCommand", "is rooted $isRooted")
        val process = Runtime.getRuntime().exec(if (isRooted) "su" else command)
        if (isRooted) {
            val dataOutputStream = DataOutputStream(process.outputStream)
            dataOutputStream.writeBytes(command + "\n")
            dataOutputStream.flush()
            dataOutputStream.writeBytes("exit\n")
            dataOutputStream.flush()
        }
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val stringBuffer = StringBuffer()
        reader.forEachLine {
            stringBuffer.append(it).append("\n")
        }
        stringBuffer.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        e.toString()
    }
}

fun isRooted(): Boolean {
    return try {
        Runtime.getRuntime().exec("su")
        true
    } catch (e: Exception) {
        false
    }
}

val Context.storageFiles: List<File>?
    get() {
        val files: MutableList<File>
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val suffix =
                getExternalFilesDir(null)?.absolutePath?.split(Environment.getExternalStorageDirectory().absolutePath)
                    ?.get(1)
            val mntFile = File("/mnt")
            files = mutableListOf()
            for (file in mntFile.listFiles()) {
                if (file.canRead() && file.canWrite()) {
                    files.add(File(file, suffix))
                }
            }
            files
        } else {
            getExternalFilesDirs(null)?.toList()
        }
    }