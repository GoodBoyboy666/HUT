package top.goodboyboy.hut.mainFragment.FragmentKb

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.gson.JsonParser
import top.goodboyboy.hut.Util.SettingsUtil
import java.io.File


class FragmentKbViewModel(private val application: Application) : AndroidViewModel(application) {

    val kbHead =
        listOf("课表", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    var zhouciSelectedIndex = 0
    var zhouciSelected = String()
    var zhouci = mutableListOf<String>()
    var kbjcmsid = String()
    var xnxq01id = String()

    /**
     * 初始化课表参数
     *
     */
    fun initKbParam() {
        val internalStorageDir = application.filesDir
        val fileName = "kbParam.txt"
        val file = File(internalStorageDir, fileName)
        val kbparam: String
        val setting = SettingsUtil(application)
        if (file.exists()) {
            kbparam = file.readText()

            val element = JsonParser.parseString(kbparam)

            val jsonObject = element.asJsonObject

            val zhouciArray = jsonObject["zhouci"].asJsonArray

            for (item in zhouciArray) {
                zhouci.add(item.asString)
            }

            kbjcmsid = jsonObject["kbjcmsid"].asString
            xnxq01id = jsonObject["xnxq01id"].asString
            if (setting.globalSettings.selectedZhouCi == -1) {
                zhouciSelected = jsonObject["zhouciSelected"].asString
                zhouciSelectedIndex = zhouciSelected.toInt() - 1
            } else {
                zhouciSelected = setting.globalSettings.selectedZhouCi.toString()
                zhouciSelectedIndex = setting.globalSettings.selectedZhouCi - 1
            }
        } else {
            Toast.makeText(application, "读取课表参数失败！", Toast.LENGTH_SHORT)
                .show()
        }
    }
}


