package top.goodboyboy.hut

import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.Serializable
import java.net.URLEncoder

class KbFunction {
    companion object {

        /**
         * 身份验证
         *
         * @param userNum 学号
         * @param userPasswd 密码
         * @param api 教务系统接口
         * @param scode scode
         * @param sxh sxh
         * @param client OkHttpClient
         * @return authStatus对象
         */
        fun authentication(
            userNum: String,
            userPasswd: String,
            api: String,
            scode: String,
            sxh: String,
            client: OkHttpClient,
            verificationCode: String
        ): AuthStatus {

//            val client = getHttpClient()

//            val encodedData = URLEncoder.encode(
//                Base64.encodeToString(
//                    userNum.toByteArray(),
//                    Base64.NO_WRAP
//                ) + "%%%" + Base64.encodeToString(userPasswd.toByteArray(), Base64.NO_WRAP), "UTF-8"
//            )

//            val codeGroup = getScode(api)
//            if (!codeGroup.isOk && codeGroup.client != null) {
//                return AuthStatus(false, codeGroup.reason ?: "", null)
//            }
//            val client = codeGroup.client

            val encodedData = URLEncoder.encode(
                encodeNumAndPasswd(
                    userNum,
                    userPasswd,
                    scode,
                    sxh
                ), "UTF-8"
            )

            val requestBodyString =
                "loginMethod=LoginToXk&userlanguage=0&userAccount=${userNum}&userPassword=&RANDOMCODE=${verificationCode}&encoded=${encodedData}"

            // 构建请求体
            val mediaType = "application/x-www-form-urlencoded".toMediaType()
            val requestBody = requestBodyString.toRequestBody(mediaType)

            // 构建请求
            val request: Request = Request.Builder()
                .url("${api}jsxsd/xk/LoginToXk")
                .post(requestBody)
                .build()

            // 发送请求并获取响应
            var response: Response? = null
            try {
                response = client.newCall(request).execute()
            } catch (e: Exception) {
                println(e.message)
            }

            // 处理响应
            if (response?.isSuccessful == true) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrBlank()) {

                    val authResultHTML: Document = Jsoup.parse(responseBody)

                    // 选择节点
                    val authResultNode: Element? = authResultHTML.selectFirst("font#showMsg")
                    return if (authResultNode != null) {
                        AuthStatus(false, authResultNode.text())
                    } else {
                        AuthStatus(true, "")
                    }
                }
            }
            return AuthStatus(false, "网络请求失败！")
        }

        /**
         * 获取scode
         *
         * @param api 教务系统接口
         * @return scode对象
         */
        fun getScode(api: String): Scode {

            val client = getHttpClient()

            val scodeRegex = Regex("""var scode\s*=\s*"(.*?)";""")
            val sxhRegex = Regex("""var sxh\s*=\s*"(.*?)";""")

            val scodeRequest: Request = Request.Builder()
                .url("${api}jsxsd/")
                .get()
                .build()
            try {
                val scodeResponse = client.newCall(scodeRequest).execute()

                if (scodeResponse.isSuccessful) {
                    val responseBody = scodeResponse.body?.string()
                    if (!responseBody.isNullOrBlank()) {
                        val scodeMatchResult = scodeRegex.find(responseBody)
                        val sxhMatchResult = sxhRegex.find(responseBody)
                        if (scodeMatchResult != null && sxhMatchResult != null) {
                            val scode = scodeMatchResult.groups[1]?.value
                            val sxh = sxhMatchResult.groups[1]?.value
                            return Scode(scode, sxh, true, null, client)
                        }
                        return Scode(null, null, false, "解析scode与sxh失败！", null)
                    }
                }
            } catch (e: Exception) {
                println(e)
            }
            return Scode(null, null, false, "请求scode与sxh失败！", null)
        }

        fun getCaptcha(api: String, client: OkHttpClient): Captcha {
            val request = Request.Builder()
                .get()
                .url("${api}jsxsd/verifycode.servlet")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val inputStream = response.body?.byteStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                return Captcha(true, bitmap)
            }
            return Captcha(false, null)
        }

        /**
         * 加密学号与密码
         *
         * @param num 学号
         * @param password 密码
         * @param scodeString scode
         * @param sxhString sxh
         * @return 加密的学号与密码
         */
        private fun encodeNumAndPasswd(
            num: String,
            password: String,
            scodeString: String,
            sxhString: String
        ): String {
            val account = Base64.encodeToString(
                num.toByteArray(),
                Base64.NO_WRAP
            )
            val passwd = Base64.encodeToString(
                password.toByteArray(),
                Base64.NO_WRAP
            )
            val code = "$account%%%$passwd"
            var scode = scodeString
            val sxh = sxhString
            val encoded = StringBuilder()

            var i = 0
            while (i < code.length) {
                if (i < 20) {
                    val codeChar = code.substring(i, i + 1)
                    val sxhValue = sxh.substring(i, i + 1).toInt()
                    encoded.append(codeChar).append(scode.substring(0, sxhValue))
                    scode = scode.substring(sxhValue)
                } else {
                    encoded.append(code.substring(i))
                    break
                }
                i++
            }
            return encoded.toString()
        }


        /**
         * 获取课表
         *
         * @param client 已通过身份验证的OkHttpClient
         * @param api 教务系统接口
         * @return kbItems对象
         */
        fun getKbData(client: OkHttpClient, api: String): KbItems {

            val allKbParam = getAllKbParam(client, api)

            if (allKbParam != null) {

                val kb = getSchedule(client, allKbParam.kbURL)
                return kb
            } else
                return KbItems(null, false, "课表链接获取失败")
        }

        /**
         * 获取课表
         *
         * @param client 已通过身份验证的OkHttpClient
         * @param zhouciSelected 选择的周次
         * @param kbjcmsid kbjcmsid
         * @param xnxq01id xnxq01id
         * @param api 教务系统接口
         * @return kbItems对象
         */
        fun getKbData(
            client: OkHttpClient,
            zhouciSelected: String,
            kbjcmsid: String,
            xnxq01id: String,
            api: String
        ): KbItems {

            val kbURL =
                "${api}jsxsd/framework/mainV_index_loadkb.htmlx?zc=$zhouciSelected&kbjcmsid=$kbjcmsid&xnxq01id=$xnxq01id&xswk=false"
            val kb = getSchedule(client, kbURL)
            return kb
        }


        /**
         * 获取全部课表参数
         *
         * @param client 已通过身份验证的OkHttpClient
         * @param api 教务系统接口
         * @return allkbParam对象（可为null）
         */
        fun getAllKbParam(client: OkHttpClient, api: String): AllKbParam? {
            val request: Request = Request.Builder()
                .url("${api}jsxsd/framework/xsMainV_new.htmlx?t1=1")
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrBlank()) {

                    val kbParam: KbParam = getKbParam(responseBody)
                    if (kbParam.zhouciSelected.isNotBlank() && kbParam.kbjcmsid.isNotBlank() && kbParam.xnxq01id.isNotBlank() && kbParam.zhouci.isNotEmpty()) {
                        val allKbParam = AllKbParam()
                        allKbParam.zhouciSelected = kbParam.zhouciSelected
                        allKbParam.zhouci =
                            kbParam.zhouci.toList().filter { it.isNotEmpty() }.toMutableList()
                        allKbParam.zhouciSelectedIndex =
                            kbParam.zhouci.indexOf(kbParam.zhouciSelected)
                        allKbParam.kbjcmsid = kbParam.kbjcmsid
                        allKbParam.xnxq01id = kbParam.xnxq01id
                        allKbParam.kbURL = getKbURL(
                            kbParam.zhouciSelected,
                            kbParam.kbjcmsid,
                            kbParam.xnxq01id,
                            api
                        )
                        return allKbParam
                    }

                }
            }
            return null
        }

        /**
         * 获取课表链接
         *
         * @param zhouciSelected 已选择的周次
         * @param kbjcmsid kbjcmsid
         * @param xnxq01id xnxq01id
         * @param api 教务系统接口
         * @return 课表链接
         */
        fun getKbURL(
            zhouciSelected: String,
            kbjcmsid: String,
            xnxq01id: String,
            api: String
        ): String {
            return "${api}jsxsd/framework/mainV_index_loadkb.htmlx?zc=$zhouciSelected&kbjcmsid=$kbjcmsid&xnxq01id=$xnxq01id&xswk=false"
        }


        /**
         * 获取课表参数
         *
         * @param responseBody 带有课表参数的html
         * @return KbParam对象
         */
        private fun getKbParam(responseBody: String): KbParam {
            val doc = Jsoup.parse(responseBody)
            val weeks: List<String> =
                doc.select("select#week option").map { it.attr("value") }.toList()


            // 获取选中周次
            val selected: Element? = doc.select("select#week option[selected]").first()

            //获取kbjcmsid
            val kbjcmsid: Element? = doc.select("ul.layui-tab-title li").first()

            //获取xnxq01id
            val xnxq01id: List<Element>? = doc.select("select[lay-filter='xnxq'] option")

            val kbParam = KbParam(
                weeks,
                kbjcmsid?.attr("data-value") ?: "",
                xnxq01id?.firstOrNull()?.text() ?: "",
                selected?.attr("value") ?: ""
            )
            return kbParam
        }

        /**
         * 获取课表内容
         *
         * @param client 已通过身份验证的OkHttpClient
         * @param kbURL 课表URL
         * @return kbItems对象
         */
        fun getSchedule(client: OkHttpClient, kbURL: String): KbItems {
            val request: Request = Request.Builder()
                .url(kbURL)
                .get()
                .build()
            val response = client.newCall(request).execute()
            return if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!(responseBody == "" || responseBody == null)) {
                    parseSchedule(responseBody)
                } else {
                    KbItems(null, false, "课表内容请求失败")
                }
            } else {
                KbItems(null, false, "网络请求失败")
            }
        }

        /**
         * 获取学生信息
         *
         * @param client OkHttpClient
         * @param api api
         * @return UserInfoClass对象（可为 null）
         */
        fun getUserInfo(client: OkHttpClient, api: String): UserInfoClass? {
            val request: Request = Request.Builder()
                .url("${api}jsxsd/framework/xsMainV_new.htmlx?t1=1")
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrBlank()) {
                    return parseUserInfo(responseBody)
                }
            }
            return null
        }

        private fun parseUserInfo(responseBody: String): UserInfoClass {

            val userInfo = UserInfoClass()
            val doc = Jsoup.parse(responseBody)

            val infoContent = doc.select("div.qz-infoContent")

            val infoContentTitleLan =
                infoContent.select("div.infoContentTitleLan.qz-flex-row.qz-ellipse")

            userInfo.userName =
                infoContentTitleLan.select("div.infoContentTitle.qz-ellipse").text() ?: "N/A"

            val infoContentBody =
                infoContent.select("div.infoContentBody > div.qz-rowlan.qz-flex-row.qz-ellipse")
            for (item in infoContentBody) {
                val attr = item.select("iconpark-icon").attr("name") ?: ""
                val info = item.select("div.qz-detailtext.qz-ellipse").text() ?: "N/A".trim()
//                val pattern = Pattern.compile("zhuanye-(\\w+)")
//                val matcher=pattern.matcher(attr)
                when (attr) {
                    "shengyuandi" -> userInfo.sourceOfOrigin = info
                    "xueyuan" -> userInfo.college = info
                    "zhuanye-a97275d0" -> userInfo.major = info
                    "shouyebanji" -> userInfo.class1 = info
                }
            }
            return userInfo
        }


        /**
         * 解析课表
         *
         * @param responseBody 带有课表内容的html
         * @return kbItems对象
         */
        private fun parseSchedule(responseBody: String): KbItems {

            // 解析 HTML
            val doc: Document = Jsoup.parse(responseBody)
            val table: Element? = doc.selectFirst("table")

            // 定义课表数组
            val tableData = mutableListOf<List<KbItem>>()

            table?.let {
                // 获取课表数据
                val tbody = it.selectFirst("tbody")
                tbody?.select("tr")?.forEach { row ->
                    val cells = row.select("td")
                    val rowData = mutableListOf<KbItem>()

                    for (cell in cells) {
                        val course = KbItem(" ", "N/A")
                        val li = cell.selectFirst("div ul li")
                        val head = cell.selectFirst("div > div.index-title")

                        if (li != null) {
                            course.title =
                                li.selectFirst("div.qz-hasCourse-title.qz-ellipse")?.text()?.trim()
                                    ?: ""

                            // 课程详细信息
                            val infoNodes = li.select("div.qz-hasCourse-detaillists > div")
                            course.infos = infoNodes.joinToString("\n") { it.text().trim() }
                        } else if (head != null) {
                            course.title = head.text().trim() + "\n"
                            course.infos = course.title +
                                    (cell.selectFirst("div > div.index-detailtext")?.text()?.trim()
                                        ?: "") + "\n" +
                                    (cell.selectFirst("div > div.index-detailtext.qz-flex-row span")
                                        ?.text()?.trim() ?: "")
                        } else {
                            course.title = " "
                            course.infos = "N/A"
                        }

                        rowData.add(course)
                    }

                    val count = cells.size
                    if (count < 8) {
                        for (i in count until 8) {
                            rowData.add(KbItem(" ", "N/A"))
                        }
                    }

                    tableData.add(rowData)
                }
            }
            return KbItems(tableData, true, null)
        }

        /**
         * 生成OkHttpClient对象
         *
         * @return OkHttpClient对象
         */
        private fun getHttpClient(): OkHttpClient {

            val userAgentInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0"
                    )
                    .build()
                chain.proceed(newRequest)
            }

            val client = OkHttpClient.Builder()
                .followRedirects(true) // 启用自动重定向
                .addInterceptor(userAgentInterceptor)
                .cookieJar(object : CookieJar {
                    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        cookieStore[url.host] = cookies.toMutableList()
                    }

                    override fun loadForRequest(url: HttpUrl): List<Cookie> {
                        return cookieStore[url.host] ?: emptyList()
                    }
                })
                .build()

            return client
        }

        /**
         * 从本地文件获取本周课表
         *
         * @param filePath 课表数据文件目录
         * @return kbItemsAsList对象
         */
        fun getKbFromFile(filePath: String): KbItemsAsList {
            val file = File(filePath)
            if (file.exists()) {
                val kbString = file.readText()
                val gson = Gson()
                val listType = object : TypeToken<List<KbItem>>() {}.type

                val zhouciKb: List<KbItem> = gson.fromJson(kbString, listType)

                return KbItemsAsList(zhouciKb, true, "")
            } else {
                return KbItemsAsList(null, false, "未找到课表文件！")
            }
        }

        /**
         * 清空文件夹
         *
         * @param directory 目录
         */
        fun clearDirectory(directory: File) {
            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()
                files?.forEach { file ->
                    if (file.isDirectory) {
                        clearDirectory(file)
                    }
                    file.delete()
                }
            }
        }

        /**
         * 检测暗色模式
         *
         * @param context 上下文
         * @return 是否为暗色模式
         */
        fun checkDarkMode(context: Context): Boolean {
            val currentNightMode =
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

            return isDarkMode
        }
    }
}

/**
 * 身份验证类
 *
 * @property status 身份验证状态
 * @property reason 原因
// * @property client 已验证的OkHttpClient（失败情况下为null）
 */
class AuthStatus
    (
    val status: Boolean,
    val reason: String,
//    val client: OkHttpClient? = null
) : Serializable

/**
 * 所有课表参数
 *
 */
class AllKbParam {
    var zhouci = mutableListOf<String>()
    var zhouciSelected = String()
    var zhouciSelectedIndex = 0
    var kbjcmsid = String()
    var xnxq01id = String()
    var kbURL = ""
}

/**
 * 课表参数
 *
 * @property zhouci 周次集合
 * @property kbjcmsid kbjcmsid
 * @property xnxq01id xnxq01id
 * @property zhouciSelected 当前周次
 */
class KbParam(
    var zhouci: List<String>,
    var kbjcmsid: String,
    var xnxq01id: String,
    var zhouciSelected: String
)

/**
 * 一周课表
 *
 * @property kbitems 课表内容
 * @property isOk 是否成功获取
 * @property reason 原因
 */
class KbItems
    (
    var kbitems: List<List<KbItem>>?,
    var isOk: Boolean,
    var reason: String?
)

/**
 * List格式存储的课表
 *
 * @property kbitems 课表信息
 * @property isOk 是否完成
 * @property reason 原因
 */
class KbItemsAsList
    (
    var kbitems: List<KbItem>?,
    var isOk: Boolean,
    var reason: String?
)

/**
 * 单个课表item
 *
 * @property title 课程名称
 * @property infos 详细
 */
class KbItem
    (
    var title: String,
    var infos: String
)

/**
 * 课表内容Item
 *
 * @property kbTitle
 * @property kbInfo
 */
class GridAdapterItems
    (
    var kbTitle: List<String>?,
    var kbInfo: List<String>?
)

/**
 * 表头Item
 *
 * @property header 表头
 */
class GridHeaderAdapterItems(
    val header: List<String>
)

/**
 * Scode内容
 *
 * @property scode
 * @property sxh
 * @property isOk
 * @property reason
 * @property client
 */
class Scode(
    val scode: String?,
    val sxh: String?,
    val isOk: Boolean,
    val reason: String?,
    val client: OkHttpClient?
)

/**
 * 验证码实体
 *
 * @property isOk 是否获取到验证码图片
 * @property image 验证码图片
 */
class Captcha(
    val isOk: Boolean,
    val image: android.graphics.Bitmap?,
)