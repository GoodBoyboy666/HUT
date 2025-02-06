package top.goodboyboy.hut.mainFragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.Activity.LoginActivity
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.SettingsUtil
import top.goodboyboy.hut.UserInfoClass
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
        val fragmentManager = requireActivity().supportFragmentManager

        binding.settingsButton.setOnClickListener {
            fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragmentContainer, SettingsFragment())
                .commit()
        }

        binding.meButtonLogout.setOnClickListener {
            logoutFun()
        }
        binding.meButtonCleanCache.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            setting.globalSettings.reCache = true
            setting.globalSettings.isLogin = false
            setting.save()
            startActivity(intent)
        }


        binding.meButtonLogoutHutApp.setOnClickListener {
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
}