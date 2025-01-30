package top.goodboyboy.hut.mainFragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.hut.CheckUpdate
import top.goodboyboy.hut.GlobalStaticMembers
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.AlertDialogUtil
import top.goodboyboy.hut.Util.BioUtil
import top.goodboyboy.hut.Util.SettingsUtil
import java.io.File

class SettingsFragment : PreferenceFragmentCompat() {
    private var cleanCacheJob: Job? = null
    private var countCacheSize:Job?=null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val isDarkMode = KbFunction.checkDarkMode(requireContext())
        val setting = SettingsUtil(requireContext())
        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            AlertDialogUtil(
                requireContext(),
                getString(R.string.about),
                getString(R.string.about_info, GlobalStaticMembers.VersionName),
                isDarkMode
            ).show()
            true
        }

        findPreference<Preference>("author_page")?.setOnPreferenceClickListener {
            val url = "https://www.goodboyboy.top"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            true
        }

        findPreference<Preference>("check_new")?.setOnPreferenceClickListener {
            if(setting.globalSettings.noMoreReminders){
                setting.globalSettings.noMoreReminders=false
                setting.save()
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val status = CheckUpdate.getLatestVersionFromGitea()
                    if (status.isSuccess) {
                        withContext(Dispatchers.Main) {
                            AlertDialogUtil(
                                requireContext(),
                                "检测到新版本" + " " + status.versionInfo?.verName,
                                status.versionInfo?.verBody ?: "未获取到更新说明",
                                isDarkMode,
                                AlertDialogUtil.AlertDialogEvent.CUSTOM
                            ) {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        status.versionInfo?.verUrl
                                            ?: "https://git.goodboyboy.top/goodboyboy/HUT"
                                    )
                                )
                                startActivity(intent)
                            }.show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            AlertDialogUtil(
                                requireContext(),
                                "提示",
                                status.reason ?: "检查更新失败！",
                                isDarkMode,
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        AlertDialogUtil(
                            requireContext(),
                            "提示",
                            e.message ?: "检查更新失败！",
                            isDarkMode,
                        ).show()
                    }
                }
            }
            true
        }

        findPreference<Preference>("Github")?.setOnPreferenceClickListener {
            val url = "https://github.com/GoodBoyboy666/HUT"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            true
        }

        val bioSwitch = findPreference<SwitchPreferenceCompat>("enable_bio")
        bioSwitch?.isChecked = setting.globalSettings.enableBio

        val executor = ContextCompat.getMainExecutor(requireContext())


        bioSwitch?.setOnPreferenceChangeListener { _, newValue ->
            val bioStatus=BioUtil().checkBiometricSupport(requireContext())
            if(bioStatus.status) {
                val isEnabled = newValue as Boolean
                if (isEnabled) {
                    val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            setting.globalSettings.enableBio = true
                            setting.save()
                            bioSwitch.isChecked = setting.globalSettings.enableBio
                        }
                    })

                    BioUtil().startAuthentication(biometricPrompt)
                    false
                } else {
                    setting.globalSettings.enableBio = false
                    setting.save()
                    true
                }
            }else{
                Toast.makeText(requireContext(),bioStatus.reason?:"未知错误",Toast.LENGTH_SHORT).show()
                false
            }
        }

        findPreference<Preference>("OpenSourceLicense")?.setOnPreferenceClickListener {
            val intent = Intent(requireContext(), OssLicensesMenuActivity::class.java)
            startActivity(intent)
            true
        }

        val cleanWebViewCache=findPreference<Preference>("clean_webView_cache")
        cleanWebViewCache?.setOnPreferenceClickListener {
            cleanCacheJob=CoroutineScope(Dispatchers.IO).launch {
                WebStorage.getInstance().deleteAllData()
                context?.deleteDatabase("webview.db")
                context?.deleteDatabase("webviewCache.db")
                val cacheDir = context?.cacheDir
                if (cacheDir != null) {
                    if (cacheDir.exists()) {
                        cacheDir.deleteRecursively()
                    }
                }
                val webViewCacheDir = context?.getDir("webview", Context.MODE_PRIVATE)
                if (webViewCacheDir!=null){
                    if (webViewCacheDir.exists()){
                        webViewCacheDir.deleteRecursively()
                    }
                }
                val codeCacheDir = File(context?.applicationInfo?.dataDir, "code_cache")
                if (codeCacheDir.exists()) {
                    codeCacheDir.deleteRecursively()
                }
                withContext(Dispatchers.Main){
                    cleanWebViewCache.summary="清理完成"
                }
            }
            true
        }
        countCacheSize= CoroutineScope(Dispatchers.IO).launch {
            val cacheDir = context?.cacheDir
            val webViewCacheDir = context?.getDir("webview", Context.MODE_PRIVATE)
            var totalCacheSize=0L
            if(cacheDir!=null) {
                totalCacheSize +=
                    calculateDirectorySize(cacheDir)
            }
            if(webViewCacheDir!=null){
                totalCacheSize +=calculateDirectorySize(webViewCacheDir)
            }
            withContext(Dispatchers.Main){
                cleanWebViewCache?.summary="缓存占用: ${formatSize(totalCacheSize)}"
            }
        }
    }

    fun calculateDirectorySize(directory: File): Long {
        if (!directory.exists()) return 0

        var size: Long = 0
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                size += if (file.isDirectory) {
                    calculateDirectorySize(file) // 递归计算子目录大小
                } else {
                    file.length() // 计算文件大小
                }
            }
        }
        return size
    }

    fun formatSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanCacheJob?.cancel()
        countCacheSize?.cancel()
    }
}