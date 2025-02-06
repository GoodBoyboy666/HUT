package top.goodboyboy.hut.Util

import android.content.Context
import com.google.gson.Gson
import top.goodboyboy.hut.SettingsClass
import java.io.File
import java.io.FileWriter

/**
 * 设置Util
 *
 * @constructor
 * 加载本地设置
 *
 * @param context 上下文
 */
class SettingsUtil(context: Context) {
    private val settingsFileName = "settings.txt"
    private val internalStorageDir: File = context.filesDir
    private val settingsFile = File(internalStorageDir, settingsFileName)
    lateinit var globalSettings: SettingsClass

    init {
        loadSettings()
    }

    private fun loadSettings() {
        if (settingsFile.exists()) {
            val settingsText = settingsFile.readText()
            if (settingsText != "") {
                globalSettings = Gson().fromJson(settingsFile.readText(), SettingsClass::class.java)
            }
        } else {
            globalSettings = SettingsClass()
            save()
        }
    }

    /**
     * 刷新设置
     *
     */
    fun flushSettings() {
        loadSettings()
    }

    /**
     * 获取设置对象
     *
     * @return SettingsClass对象
     */
    fun getSettings(): SettingsClass {
        return globalSettings
    }

    /**
     * 保存设置
     *
     */
    fun save() {
        val writer = FileWriter(settingsFile, false)
        writer.write(Gson().toJson(globalSettings))
        writer.close()
    }
}