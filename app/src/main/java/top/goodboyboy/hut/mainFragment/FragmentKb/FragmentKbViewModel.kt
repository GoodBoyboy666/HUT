package top.goodboyboy.hut.mainFragment.FragmentKb

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.google.gson.JsonParser
import java.io.File


class FragmentKbViewModel(private val application: Application) : AndroidViewModel(application) {

    var allitems = List(40) { "" }.toMutableList()
    var allinfos = List(40) { "N/A" }.toMutableList()
    val kbHead =
        listOf("课表", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    var isFirst = true

    //    val message=MutableLiveData<String>()
    var zhouciSelectedIndex = 0
    var zhouciSelected = String()
    var zhouci = mutableListOf<String>()
    var kbjcmsid = String()
    var xnxq01id = String()

//    var userNum=String()
//    var userPasswd=String()

    init {

        initKbParam()

//        initUserPasswd()
    }

    private fun initKbParam() {
        val internalStorageDir = application.filesDir
        val fileName = "kbParam.txt"
        val file = File(internalStorageDir, fileName)
        val kbparam: String
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
            zhouciSelected = jsonObject["zhouciSelected"].asString
            zhouciSelectedIndex = zhouciSelected.toInt() - 1
        } else {
            Toast.makeText(application, "读取课表参数失败，建议手动清除数据！", Toast.LENGTH_LONG)
                .show()
        }
    }

//    fun initUserPasswd(){
//        val internalStorageDir = application.filesDir
//        val fileName = "data.txt"
//        val file = File(internalStorageDir, fileName)
//        val userInfo: String
//        if (file.exists()) {
//            userInfo=file.readText()
//            userNum=userInfo.split(':')[0]
//            userPasswd=userInfo.split(':')[1]
//        }
//        else
//        {
//            Toast.makeText(application,"未找到用户配置文件，建议重新登录！",Toast.LENGTH_LONG).show()
//        }
//    }
}


