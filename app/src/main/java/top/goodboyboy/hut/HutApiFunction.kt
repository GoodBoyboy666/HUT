package top.goodboyboy.hut


import com.google.gson.JsonParser

import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import kotlin.random.Random

class HutApiFunction {
    companion object {
        fun getAccessToken(
            client: OkHttpClient,
            userName: String,
            password: String,
            appId: String,
            deviceId: String,
            osType: String,
            clientId: String
        ): AccessToken {
            val requestBodyString = ""
            val mediaType = "application/x-www-form-urlencoded".toMediaType()
            val requestBody = requestBodyString.toRequestBody(mediaType)

            val accessTokenRequest = Request.Builder()
                .url("https://mycas.hut.edu.cn/token/password/passwordLogin?username=${userName}&password=${password}&appId=${appId}&geo=&deviceId=${deviceId}&osType=${osType}&clientId=${clientId}&mfaState=")
                .post(requestBody)
                .build()

            try {
                val response = client.newCall(accessTokenRequest).execute()
                if (response.isSuccessful) {
                    val responseJson = response.body?.string()
                    val accessToken =
                        JsonParser.parseString(responseJson).asJsonObject.get("data").asJsonObject.get(
                            "idToken"
                        ).asString
                    return AccessToken(true, accessToken, null)
                }
                return AccessToken(false, null, "请求失败！" + response.code + response.message)
            } catch (e: Exception) {
                return AccessToken(false, null, e.message)
            }
        }

        fun generateRandomString(charPool: List<Char>, length: Int): String {
            return (1..length)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }

        fun getServiceList(accessToken: String): ServiceList {

//            ///only for test
//            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
//                override fun checkClientTrusted(
//                    chain: Array<out X509Certificate>?,
//                    authType: String?
//                ) {
//                }
//
//                override fun checkServerTrusted(
//                    chain: Array<out X509Certificate>?,
//                    authType: String?
//                ) {
//                }
//
//                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
//            })
//
//            val sslContext = SSLContext.getInstance("SSL")
//            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
//            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
//
//            val client = OkHttpClient.Builder()
//                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
//                .hostnameVerifier { _, _ -> true }
//                .build()
//            ///

            val client = OkHttpClient()

            val requestBodyString = ""
            val mediaType = "application/json".toMediaType()
            val requestBody = requestBodyString.toRequestBody(mediaType)

            val serviceListRequest = Request.Builder()
                .url("https://portal.hut.edu.cn/portal-api/v1/service/list")
                .post(requestBody)
                .header("X-Id-Token", accessToken)
                .header("User-Agent", "OKHttpClient")
                .header("X-Terminal-Info", "app")
                .build()
            val response = client.newCall(serviceListRequest).execute()
            if (response.isSuccessful) {
                val responseJson = response.body?.string()
                val jsonObject = JsonParser.parseString(responseJson).asJsonObject
                val codeStatus =
                    jsonObject.get("code").asInt
                return when (codeStatus) {
                    0 -> {
                        ServiceList(true, responseJson, null)
                    }

                    -1 -> {
                        ServiceList(false, null, "获取失败，可能由于令牌已失效，请重新登录")
                    }

                    else -> {
                        ServiceList(false, null, jsonObject.get("message").asString)
                    }
                }
            }
            return ServiceList(false, null, "请求失败！")
        }

        fun parseTokenType(typeJson: String): TokenTypeName {
            var headerTokenKeyName = ""
            var urlTokenKeyName = ""
            if (typeJson != "") {
                val typeJsonObject = JsonParser.parseString(typeJson)
                for (type in typeJsonObject.asJsonArray) {
                    if (type.asJsonObject.get("tokenType").asString == "header") {
                        val header = type.asJsonObject.get("tokenKey").asString
                        if (header.contains('=')) {
                            headerTokenKeyName = header.split('=')[0]
                        } else {
                            headerTokenKeyName = header
                        }
                    } else if (type.asJsonObject.get("tokenType").asString == "url") {
                        val url = type.asJsonObject.get("tokenKey").asString
                        if (url.contains('=')) {
                            urlTokenKeyName = url.split('=')[0]
                        } else {
                            urlTokenKeyName = url
                        }
                    }
                }
            }
            return TokenTypeName(headerTokenKeyName, urlTokenKeyName)
        }
    }
}

class AccessToken(
    val isOk: Boolean,
    val accessToken: String?,
    val errorMessage: String?
)

class ServiceList(
    val isOk: Boolean,
    val serviceList: String?,
    val errorMessage: String?
)

class ServiceItem(
    val imageUrl: String,
    val text: String,
    val serviceUrl: String,
    val tokenAccept: String
)

class TokenTypeName(
    val headerTokenKeyName: String,
    val urlTokenKeyName: String
)