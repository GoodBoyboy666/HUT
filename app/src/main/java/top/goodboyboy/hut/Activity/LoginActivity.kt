package top.goodboyboy.hut.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import top.goodboyboy.hut.AuthStatus
import top.goodboyboy.hut.CheckUpdate
import top.goodboyboy.hut.GlobalStaticMembers
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.Scode
import top.goodboyboy.hut.Util.AlertDialogUtil
import top.goodboyboy.hut.Util.SettingsUtil
import top.goodboyboy.hut.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var codeList: Scode
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        DynamicColors.applyToActivitiesIfAvailable(application)
        setContentView(view)


        val isDarkMode = KbFunction.checkDarkMode(this)


        //初始化设置

        val setting = SettingsUtil(this)


        binding.progressRelativeLayout.visibility = View.GONE

        //检测是否已经登录
        if (setting.globalSettings.isLogin) {
            if (setting.globalSettings.enableBio) {
                val intent = Intent(this, BioActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                val intent = Intent(this, MainActivityPage::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        } else {
            //检测是否存在账号信息并读取
            binding.userNum.setText(setting.globalSettings.userNum)
            binding.userPasswd.setText(setting.globalSettings.userPasswd)
            GlobalStaticMembers.apiSelected = setting.globalSettings.selectedAPI

            //检测更新
            if (!setting.globalSettings.noMoreReminders) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val status = CheckUpdate.getLatestVersionFromGitea()
                        withContext(Dispatchers.Main) {
                            if (status.isSuccess) {
                                AlertDialogUtil(
                                    this@LoginActivity,
                                    "检测到新版本" + " " + status.versionInfo?.verName,
                                    status.versionInfo?.verBody ?: "未获取到更新说明",
                                    isDarkMode,
                                    AlertDialogUtil.AlertDialogEvent.CUSTOM,
                                    AlertDialogUtil.AlertDialogType.NEW_VERSION
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
                        }
                    } catch (_: Exception) {

                    }
                }
            }

            //初始化线路选择
            val spinnerAdapter =
                ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    GlobalStaticMembers.jwxtAPI
                )
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

            //初始化scode
            scode()


            //验证码刷新事件
            binding.captchaImage.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (codeList.client != null) {
                            val captcha = KbFunction.getCaptcha(
                                GlobalStaticMembers.jwxtAPI[GlobalStaticMembers.apiSelected],
                                codeList.client!!
                            )

                            withContext(Dispatchers.Main) {
                                binding.captchaImage.setImageBitmap(captcha.image)
                            }
                        } else {
                            scode()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@LoginActivity,
                                e.message ?: "获取验证码失败！",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            //登录按钮事件绑定
            binding.buttonLogin.setOnClickListener {
                binding.progressRelativeLayout.visibility = View.VISIBLE

                if (::codeList.isInitialized && !codeList.scode.isNullOrBlank() && !codeList.sxh.isNullOrBlank() && codeList.client != null) {

                    CoroutineScope(Dispatchers.Main).launch {
                        val auth: AuthStatus
                        withContext(Dispatchers.IO) {
                            auth = checkLogin(
                                codeList.scode!!,
                                codeList.sxh!!,
                                binding.verificationCode.text.toString(),
                                codeList.client!!
                            )
                        }
                        if (auth.status) {
                            //将账号密码与设置写入文件
                            GlobalStaticMembers.client = codeList.client
                            binding.progressRelativeLayout.visibility = View.GONE

                            setting.globalSettings.userNum = binding.userNum.text.toString()
                            setting.globalSettings.userPasswd = binding.userPasswd.text.toString()
                            setting.globalSettings.selectedAPI =
                                binding.chooseAPI.selectedItemPosition
                            setting.globalSettings.reCache = false
                            setting.globalSettings.isLogin = true
                            setting.save()

                            val intent = Intent(this@LoginActivity, CacheActivity::class.java)
                            startActivity(intent)
                        } else {
                            binding.progressRelativeLayout.visibility = View.GONE
                            Toast.makeText(this@LoginActivity, auth.reason, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                } else {
                    binding.progressRelativeLayout.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "未获取到scode！", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    /**
     * 获取scode
     *
     */
    private fun scode() {
        //获取登录所需参数
        CoroutineScope(Dispatchers.IO).launch {
            codeList =
                KbFunction.getScode(GlobalStaticMembers.jwxtAPI[GlobalStaticMembers.apiSelected])

            if (codeList.isOk && codeList.client != null) {
                val captcha = KbFunction.getCaptcha(
                    GlobalStaticMembers.jwxtAPI[GlobalStaticMembers.apiSelected],
                    codeList.client!!
                )
                withContext(Dispatchers.Main) {
                    binding.captchaImage.setImageBitmap(captcha.image)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        codeList.reason ?: "获取scode失败",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }

        }
    }

    /**
     * 登录
     *
     * @param scode scode参数
     * @param sxh sxh参数
     * @param verificationCode 验证码
     * @param client 参与验证的OkHttpClient对象
     * @return AuthStatus对象
     */
    private fun checkLogin(
        scode: String,
        sxh: String,
        verificationCode: String,
        client: OkHttpClient
    ): AuthStatus {
        return KbFunction.authentication(
            binding.userNum.text.toString(),
            binding.userPasswd.text.toString(),
            GlobalStaticMembers.jwxtAPI[GlobalStaticMembers.apiSelected],
            scode,
            sxh,
            client,
            verificationCode
        )
    }

}