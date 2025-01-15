package top.goodboyboy.hut.Util

import android.content.Context
import com.google.gson.Gson
import top.goodboyboy.hut.SettingsClass
import java.io.File
import java.io.FileWriter

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

    fun flushSettings() {
        loadSettings()
    }

    fun getSettings(): SettingsClass {
        return globalSettings
    }

    fun save() {
        val writer = FileWriter(settingsFile, false)
        writer.write(Gson().toJson(globalSettings))
        writer.close()
    }
}