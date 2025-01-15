package top.goodboyboy.hut.mainFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.hut.CheckUpdate
import top.goodboyboy.hut.GlobalStaticMembers
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.Activity.LoginActivity
import top.goodboyboy.hut.Activity.MainActivityPage
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.SettingsUtil
import top.goodboyboy.hut.UserInfoClass
import top.goodboyboy.hut.Util.AlertDialogUtil
import top.goodboyboy.hut.databinding.FragmentMeBinding
import java.io.File

class FragmentMe : Fragment() {
    private var _binding: FragmentMeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val internalStorageDir = requireContext().filesDir
        val isDarkMode = KbFunction.checkDarkMode(requireContext())
        val setting = SettingsUtil(requireContext())

        var buttonBackground = R.drawable.hut_getkb_button
        if (isDarkMode) {
            buttonBackground = R.color.grey
        }

        binding.meButtonAbout.setBackgroundResource(buttonBackground)
        binding.meButtonAuthorPage.setBackgroundResource(buttonBackground)
        binding.meButtonCleanCache.setBackgroundResource(buttonBackground)
        binding.meButtonLogout.setBackgroundResource(buttonBackground)
        binding.meButtonCheckNew.setBackgroundResource(buttonBackground)
        binding.meButtonLogoutHutApp.setBackgroundResource(buttonBackground)

        binding.meButtonLogout.setOnClickListener {
            logoutFun()
        }
        binding.meButtonAbout.setOnClickListener {
//            MainActivityPage.showAlertDialog(
//                requireContext(),
//                getString(R.string.about),
//                getString(R.string.about_info, GlobalStaticMembers.VersionName),
//                isDarkMode
//            )

            AlertDialogUtil(
                requireContext(),
                getString(R.string.about),
                getString(R.string.about_info, GlobalStaticMembers.VersionName),
                isDarkMode
            ).show()
        }
        binding.meButtonAuthorPage.setOnClickListener {
            val url = "https://www.goodboyboy.top"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.meButtonCleanCache.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            setting.globalSettings.reCache = true
            setting.globalSettings.isLogin = false
            setting.save()
            startActivity(intent)

//            val fileName = "settings.txt"
//            val file = File(internalStorageDir, fileName)
//
//            if (file.exists()) {
//                val fileText = file.readText()
//                if (fileText != "") {
//                    val settings = Gson().fromJson(file.readText(), SettingsClass::class.java)
//                    settings.reCache = true
//                    val writer = FileWriter(file, false)
//                    writer.write(Gson().toJson(settings))
//                    writer.close()
//                    startActivity(intent)
//                }
//            } else {
//                Toast.makeText(
//                    requireContext(),
//                    "配置文件不存在，请使用注销功能！",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
        }

        binding.meButtonCheckNew.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val status = CheckUpdate.getLatestVersionFromGitea()
                    if (status.isSuccess) {
//                    showNewVersionAlertDialog(
//                        requireContext(),
//                        "检测到新版本" + " " + status.versionInfo?.verName,
//                        status.versionInfo?.verBody ?: "未获取到更新说明",
//                        status.versionInfo?.verUrl ?: "https://git.goodboyboy.top/goodboyboy/HUT",
//                        isDarkMode
//                    )
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
//                    MainActivityPage.showAlertDialog(
//                        requireContext(),
//                        "提示",
//                        status.reason ?: "检查更新失败！",
//                        isDarkMode
//                    )
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
        }

        binding.meButtonLogoutHutApp.setOnClickListener {
//            val fileName = "settings.txt"
//            val file = File(internalStorageDir, fileName)
//
//            if (file.exists()) {
//                val fileText = file.readText()
//                if (fileText != "") {
//                    val settings = Gson().fromJson(file.readText(), SettingsClass::class.java)
//                    settings.accessToken = ""
//                    val writer = FileWriter(file, false)
//                    writer.write(Gson().toJson(settings))
//                    writer.close()
//                    Toast.makeText(
//                        requireContext(),
//                        "清除完成！",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            } else {
//                Toast.makeText(
//                    requireContext(),
//                    "配置文件不存在，请使用注销功能！",
//                    Toast.LENGTH_LONG
//                ).show()
//            }

            setting.globalSettings.accessToken = ""
            setting.save()
            Toast.makeText(
                requireContext(),
                "清除完成！",
                Toast.LENGTH_LONG
            ).show()
        }


        val fileName = "userInfo.txt"
        val file = File(internalStorageDir, fileName)
        if (file.exists()) {
            val userString = file.readText()
            val userObj = Gson().fromJson(userString, UserInfoClass::class.java)
            val name = userObj.userName.split('-')[0]
            val num = userObj.userName.split('-')[1]
            binding.userName.text = "${name}\n${num}"
            binding.userInfo.text = userObj.sourceOfOrigin + "\n" +
                    userObj.college + "\n" +
                    userObj.major + "\n" +
                    userObj.class1
        } else {
            Toast.makeText(context, "未找到用户信息文件！", Toast.LENGTH_LONG).show()
        }


    }

    private fun logoutFun() {
        val internalStorageDir = requireContext().filesDir
        KbFunction.clearDirectory(internalStorageDir)
        Toast.makeText(context, "注销完成！", Toast.LENGTH_LONG).show()
        finishAffinity(requireActivity())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    fun showNewVersionAlertDialog(
//        context: Context,
//        title: String,
//        message: String,
//        url: String,
//        isDark: Boolean
//    ) {
//        val builder = AlertDialog.Builder(context)
//        val inflater = LayoutInflater.from(context)
//        val dialogView: View = inflater.inflate(R.layout.custom_dialog, null)
//        val dialog: LinearLayout = dialogView.findViewById(R.id.custom_alertdialog)
//        val titleTextView: TextView = dialogView.findViewById(R.id.dialog_title)
//        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
//        val positiveButton: Button = dialogView.findViewById(R.id.dialog_positiveButton)
//        var dialogBackground = R.drawable.info_border
//        var okBackground = R.drawable.kb_ok
//        titleTextView.text = title
//        messageTextView.text = message
//
//        if (isDark) {
//            dialogBackground = R.color.grey
//            okBackground = R.color.grey
//        }
//        dialog.setBackgroundResource(dialogBackground)
//        positiveButton.setBackgroundResource(okBackground)
//        builder.setView(dialogView)
//        val alertDialog = builder.create()
//        positiveButton.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            startActivity(intent)
//        }
//        alertDialog.show()
//    }
}