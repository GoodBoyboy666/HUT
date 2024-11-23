package top.goodboyboy.hut

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.hut.databinding.ActivityLoginBinding
import java.io.File
import java.io.FileWriter

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        UncaughtException.getInstance(this)

        var pageBackground = R.drawable.hut_main_kb_background
        var buttonBackground = R.drawable.hut_getkb_button

        //暗色模式判定
        if (KbFunction.checkDarkMode(this)) {
            pageBackground = R.color.black
            buttonBackground = R.color.grey
        }
        binding.main.setBackgroundResource(pageBackground)
        binding.buttonLogin.setBackgroundResource(buttonBackground)

        val internalStorageDir = application.filesDir

        //重新加载课表判定
        val extras = intent.extras
        if (extras != null) {
            val reCache = extras.getBoolean("ReCache")
            if (reCache) {
                binding.progressRelativeLayout.visibility = View.VISIBLE
                val fileName = "settings.txt"
                val file = File(internalStorageDir, fileName)
                val userInfo = Gson().fromJson(file.readText(), SettingsClass::class.java)
                binding.userNum.setText(userInfo.userNum)
                binding.userPasswd.setText(userInfo.userPasswd)
                GlobalStaticMembers.apiSelected = userInfo.selectedAPI

                CoroutineScope(Dispatchers.Main).launch {
                    val auth: AuthStatus
                    withContext(Dispatchers.IO) {
                        auth = checkLogin()
                    }
                    if (auth.status) {
                        GlobalStaticMembers.client = auth.client
                        binding.progressRelativeLayout.visibility = View.GONE
                        val kbDir = internalStorageDir.path + "/kbs/"
                        KbFunction.clearDirectory(File(kbDir))
                        val intent = Intent(this@LoginActivity, CacheActivity::class.java)
                        startActivity(intent)
                    } else {
                        binding.progressRelativeLayout.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, auth.reason, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            //检测是否存在设置文件
            val fileName = "settings.txt"
            val file = File(internalStorageDir, fileName)
            val userInfo: String
            if (file.exists()) {
                userInfo = file.readText()
                if (userInfo != "") {
                    val intent = Intent(this, MainActivityPage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }

        //初始化线路选择
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, GlobalStaticMembers.jwxtAPI)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.chooseAPI.adapter = spinnerAdapter
        binding.chooseAPI.setSelection(GlobalStaticMembers.apiSelected)
        binding.chooseAPI.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                GlobalStaticMembers.apiSelected = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        //登录按钮事件绑定
        binding.buttonLogin.setOnClickListener {
            binding.progressRelativeLayout.visibility = View.VISIBLE

            KbFunction.clearDirectory(internalStorageDir)

            CoroutineScope(Dispatchers.Main).launch {
                val auth: AuthStatus
                withContext(Dispatchers.IO) {
                    auth = checkLogin()
                }
                if (auth.status) {
                    //将账号密码与设置写入文件
                    GlobalStaticMembers.client = auth.client
                    binding.progressRelativeLayout.visibility = View.GONE

                    val settingsClass = SettingsClass(
                        binding.userNum.text.toString(),
                        binding.userPasswd.text.toString(),
                        selectedAPI = binding.chooseAPI.selectedItemPosition
                    )

                    val fileName = "settings.txt"
                    val file = File(internalStorageDir, fileName)
                    val writer = FileWriter(file, false)
                    writer.write(Gson().toJson(settingsClass))
                    writer.close()
                    val intent = Intent(this@LoginActivity, CacheActivity::class.java)
                    startActivity(intent)
                } else {
                    binding.progressRelativeLayout.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, auth.reason, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkLogin(): AuthStatus {
//        return MainFunction(MainFunction.getHttpClient(),this).authentication(binding.userNum.text.toString(),binding.userPasswd.text.toString())
        return KbFunction.authentication(
            binding.userNum.text.toString(),
            binding.userPasswd.text.toString(),
            GlobalStaticMembers.jwxtAPI[GlobalStaticMembers.apiSelected]
        )
    }
}