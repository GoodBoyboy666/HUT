package top.goodboyboy.hut

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class CheckUpdate {
    companion object {
        /**
         * 从Gitea检查版本信息
         *
         * @return 包含版本信息的对象
         */
        suspend fun getLatestVersionFromGitea(): VersionStatus {
            var response: Response
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder()
                .get()
                .url("https://git.goodboyboy.top/api/v1/repos/goodboyboy/HUT/releases/latest")
                .build()
            withContext(Dispatchers.IO) {
                response = client.newCall(request).execute()
            }
            if (response.isSuccessful) {
                val responseText = response.body?.string()
                val jsonElement = JsonParser.parseString(responseText)
                if (jsonElement.isJsonObject) {
                    val jsonObj = jsonElement.asJsonObject
                    val newVersion =
                        checkNum(jsonObj.get("name").asString, GlobalStaticMembers.VersionName)
                    if (newVersion) {
                        val verName = jsonObj.get("name").asString
                        val verBody = jsonObj.get("body").asString.replace("\r\n", "\n")
                        val verUrl = jsonObj.get("html_url").asString
                        return VersionStatus(true, VersionInfo(verName, verBody, verUrl), "")
                    } else {
                        return (VersionStatus(false, null, "已是最新版本！"))
                    }
                } else {
                    return (VersionStatus(false, null, "解析Json失败！"))
                }
            } else {
                return (VersionStatus(false, null, "获取更新信息失败！"))
            }
        }


        fun getLatestVersionFromGithub() {
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder()
                .get()
        }

        /**
         * 比较版本
         *
         * @param latest 最新版本（带v）
         * @param now 当前版本信息（不带v）
         * @return
         */
        fun checkNum(latest: String, now: String): Boolean {
            val latestVer = latest.split('v')[1]
            if (latestVer.split('.')[0] <= now.split('.')[0]) {
                if (latestVer.split('.')[1] <= now.split('.')[1]) {
                    if (latestVer.split('.')[2] <= now.split('.')[2]) {
                        return false
                    }
                }
            }
            return true
        }
    }
}

/**
 * 版本信息对象
 *
 * @property isSuccess 是否有新版本
 * @property versionInfo 版本信息
 * @property reason 原因
 */
class VersionStatus(
    var isSuccess: Boolean,
    var versionInfo: VersionInfo?,
    var reason: String?
)

/**
 * 版本信息
 *
 * @property verName 版本号
 * @property verBody 新版本更新内容
 * @property verUrl 新版本页面
 */
class VersionInfo(
    var verName: String,
    var verBody: String,
    var verUrl: String
)