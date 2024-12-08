package top.goodboyboy.hut

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        val isDarkMode = KbFunction.checkDarkMode(requireContext())

        var buttonBackground = R.drawable.hut_getkb_button
        if (isDarkMode) {
            buttonBackground = R.color.grey
        }

        binding.meButtonAbout.setBackgroundResource(buttonBackground)
        binding.meButtonAuthorPage.setBackgroundResource(buttonBackground)
        binding.meButtonCleanCache.setBackgroundResource(buttonBackground)
        binding.meButtonLogout.setBackgroundResource(buttonBackground)
        binding.meButtonCheckNew.setBackgroundResource(buttonBackground)

        binding.meButtonLogout.setOnClickListener {
            logoutFun()
        }
        binding.meButtonAbout.setOnClickListener {
            MainActivityPage.showAlertDialog(
                requireContext(),
                getString(R.string.about),
                getString(R.string.about_info,GlobalStaticMembers.VersionName),
                isDarkMode
            )
        }
        binding.meButtonAuthorPage.setOnClickListener {
            val url = "https://www.goodboyboy.top"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        binding.meButtonCleanCache.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val bundle = Bundle()
            bundle.putBoolean("ReCache", true)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        binding.meButtonCheckNew.setOnClickListener{
            CoroutineScope(Dispatchers.Main).launch {
                val status = CheckUpdate.getLatestVersionFromGitea()
                if (status.isSuccess) {
                    MainActivityPage.showAlertDialog(
                        requireContext(),
                        "检测到新版本" + status.versionInfo?.verName,
                        status.versionInfo?.verBody ?: "未获取到更新说明",
                        isDarkMode
                    )
                } else {
                    MainActivityPage.showAlertDialog(
                        requireContext(),
                        "提示",
                        status.reason ?: "检查更新失败！",
                        isDarkMode
                    )
                }
            }
        }

        val internalStorageDir = requireContext().filesDir
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
}