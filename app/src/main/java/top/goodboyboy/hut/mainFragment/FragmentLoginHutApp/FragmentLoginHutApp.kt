package top.goodboyboy.hut.mainFragment.FragmentLoginHutApp

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import top.goodboyboy.hut.HutApiFunction
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.R
import top.goodboyboy.hut.SettingsClass
import top.goodboyboy.hut.databinding.FragmentLoginHutAppBinding
import top.goodboyboy.hut.mainFragment.FragmentHutServiceCenter.FragmentHutServiceCenter
import java.io.File
import java.io.FileWriter

class FragmentLoginHutApp : Fragment() {

    private var _binding: FragmentLoginHutAppBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FragmentLoginHutAppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginHutAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isDarkMode = KbFunction.checkDarkMode(requireContext())
        var buttonBackground = R.drawable.hut_getkb_button
        val internalStorageDir = requireContext().filesDir
        val fragmentManager = requireActivity().supportFragmentManager

        //暗色模式判定
        if (isDarkMode) {
            buttonBackground = R.color.grey
        }

        Toast.makeText(requireContext(), "该账号密码为智慧工大账户账号密码！", Toast.LENGTH_SHORT)
            .show()

        binding.buttonLogin.setBackgroundResource(buttonBackground)

        binding.buttonLogin.setOnClickListener {
            binding.buttonLogin.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            val deviceIdCharPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val clientIdCharPool: List<Char> = ('a'..'z') + ('0'..'9')

            val client = OkHttpClient()
            val userName = binding.userNum.text.toString()
            val password = binding.userPasswd.text.toString()
            val appId = "com.supwisdom.hut"
            val deviceId =
                HutApiFunction.generateRandomString(deviceIdCharPool, 24)//随机生成设备ID，避免被跟踪（雾）
            val osType = "android"
            val clientId = HutApiFunction.generateRandomString(clientIdCharPool, 32)

            CoroutineScope(Dispatchers.IO).launch {
                val accessToken = HutApiFunction.getAccessToken(
                    client,
                    userName,
                    password,
                    appId,
                    deviceId,
                    osType,
                    clientId
                )
                if (accessToken.isOk && accessToken.accessToken != null) {
                    val fileName = "settings.txt"
                    val file = File(internalStorageDir, fileName)

                    if (file.exists()) {
                        val fileText = file.readText()
                        if (fileText != "") {
                            val settings =
                                Gson().fromJson(file.readText(), SettingsClass::class.java)
                            settings.accessToken = accessToken.accessToken

                            val writer = FileWriter(file, false)
                            writer.write(Gson().toJson(settings))
                            writer.close()

                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT)
                                    .show()
                                fragmentManager.beginTransaction()
                                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                    .replace(R.id.fragmentContainer, FragmentHutServiceCenter())
                                    .commit()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "获取配置文件异常，建议退出登录后重新登录！",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            accessToken.errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            binding.buttonLogin.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}