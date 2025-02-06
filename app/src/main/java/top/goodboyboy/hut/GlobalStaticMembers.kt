package top.goodboyboy.hut

import okhttp3.OkHttpClient

class GlobalStaticMembers {
    companion object {
        var client: OkHttpClient? = null
        const val mainAPI = "http://jwxt.hut.edu.cn/"
        const val backupAPI = "http://jwxt.hut.edu.cn:83/"
        val jwxtAPI = listOf(mainAPI, backupAPI)
        var apiSelected = 0
        var VersionName = "1.2.5"
    }
}