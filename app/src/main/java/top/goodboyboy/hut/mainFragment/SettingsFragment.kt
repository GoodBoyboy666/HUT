package top.goodboyboy.hut.mainFragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.hut.CheckUpdate
import top.goodboyboy.hut.GlobalStaticMembers
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.AlertDialogUtil
import top.goodboyboy.hut.Util.SettingsUtil

class SettingsFragment : PreferenceFragmentCompat() {
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
                }catch (e:Exception){
                    withContext(Dispatchers.Main){
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
    }
}