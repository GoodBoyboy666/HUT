package top.goodboyboy.hut

data class SettingsClass(
    var userNum: String = "",
    var userPasswd: String = "",
    var selectedAPI: Int = 0,
    var reCache: Boolean = false,
    var accessToken: String = "",
    var isLogin: Boolean = false,
    var enableBio: Boolean = false,
    var noMoreReminders:Boolean=false
)

data class UserInfoClass(
    var userName: String = "N/A",
    var sourceOfOrigin: String = "N/A",
    var college: String = "N/A",
    var major: String = "N/A",
    var class1: String = "N/A"
)