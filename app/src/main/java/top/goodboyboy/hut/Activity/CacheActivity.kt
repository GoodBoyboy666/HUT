package top.goodboyboy.hut.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import top.goodboyboy.hut.GlobalStaticMembers
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.KbItem
import top.goodboyboy.hut.KbItems
import top.goodboyboy.hut.KbParam
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.Hash
import top.goodboyboy.hut.databinding.ActivityCacheBinding
import java.io.File

class CacheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCacheBinding

    private var authClient = GlobalStaticMembers.client

    private val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCacheBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        //背景设置
        var background = R.drawable.hut_main_kb_background

        val isDarkMode = KbFunction.checkDarkMode(this)

        if (isDarkMode) {
            background = R.color.grey
        }

        //设置背景
        binding.main.setBackgroundResource(background)

        if (authClient == null) {
            Toast.makeText(this, "获取Client出现异常，请联系开发人员！", Toast.LENGTH_LONG).show()
            finish()
        }
        //开始缓存课表
        CoroutineScope(Dispatchers.Main + job).launch {
            withContext(Dispatchers.IO) {
                authClient?.let { storeUserInfo(it) }
                authClient?.let { cacheKb(it) }
            }
            val intent = Intent(this@CacheActivity, MainActivityPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    /**
     * 缓存课表
     *
     * @param client 已完成身份验证的OkHttpClient
     */
    private suspend fun cacheKb(client: OkHttpClient) {
        val allkbParam = KbFunction.getAllKbParam(
            client,
            GlobalStaticMembers.jwxtAPI[GlobalStaticMembers.apiSelected]
        )
        if (allkbParam != null) {
            var count = 0
            val zhouciCount = allkbParam.zhouci.size
            for (item in allkbParam.zhouci) {
                val kbitems =
                    KbFunction.getKbData(
                        client,
                        item,
                        allkbParam.kbjcmsid,
                        allkbParam.xnxq01id,
                        GlobalStaticMembers.jwxtAPI[GlobalStaticMembers.apiSelected]
                    )
                if (kbitems.isOk) {
                    storeKb(kbitems, item, allkbParam.kbjcmsid, allkbParam.xnxq01id)
                    withContext(Dispatchers.Main) {
                        binding.cacheProgressText.text =
                            getString(R.string.cache_kb_placeholder, "(${count}/${zhouciCount})")
                    }
                    count++

                } else {
                    Toast.makeText(this, kbitems.reason, Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            cacheKbParam(
                allkbParam.zhouci.toList(),
                allkbParam.kbjcmsid,
                allkbParam.xnxq01id,
                allkbParam.zhouciSelected
            )
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CacheActivity, "获取课表参数失败！", Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }

    /**
     * 保存课表信息
     *
     * @param kb 课表Item
     * @param zhouciSelected 已选择的周次
     * @param kbjcmsid kbjcmsid
     * @param xnxq01id xnxq01id
     */
    private suspend fun storeKb(
        kb: KbItems,
        zhouciSelected: String,
        kbjcmsid: String,
        xnxq01id: String
    ) {

        val zhouKb = mutableListOf<KbItem>()
        for (item in kb.kbitems!!) {
            for (kbi in item) {
                val kbitem = KbItem(kbi.title, kbi.infos)
                zhouKb.add(kbitem)
            }
        }


        val kbStorageDir = "${application.filesDir}/kbs"
        val fileName = Hash.hash(
            zhouciSelected +
                    kbjcmsid +
                    xnxq01id
        )
        val file = File(kbStorageDir, fileName)

        file.parentFile?.mkdirs()
        if (file.exists()) {
            file.delete()
        }

        try {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.IO) {
                Toast.makeText(this@CacheActivity, "无法创建缓存！", Toast.LENGTH_LONG).show()
            }
            finish()
        }

        val gson = Gson()

        val json = gson.toJson(zhouKb)

        file.writeText(json)

    }

    /**
     * 缓存课表参数
     *
     * @param zhouci 周次信息
     * @param kbjcmsid kbjcmsid
     * @param xnxq01id xnxq01id
     * @param zhouciSelected 已选择的周次
     */
    private suspend fun cacheKbParam(
        zhouci: List<String>,
        kbjcmsid: String,
        xnxq01id: String,
        zhouciSelected: String
    ) {
        val internalStorageDir = application.filesDir
        val fileName = "kbParam.txt"
        val file = File(internalStorageDir, fileName)
        if (file.exists()) {
            file.delete()
        }
        try {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.IO) {
                Toast.makeText(this@CacheActivity, "写入课表参数失败！", Toast.LENGTH_LONG).show()
            }
            finish()
        }

        val gson = Gson()

        val json = gson.toJson(KbParam(zhouci, kbjcmsid, xnxq01id, zhouciSelected))

        file.writeText(json)

    }

    /**
     * 缓存用户信息
     *
     * @param client 已通过身份验证的OkHttpClient
     */
    private suspend fun storeUserInfo(client: OkHttpClient) {
        val userInfo = KbFunction.getUserInfo(
            client,
            GlobalStaticMembers.jwxtAPI[GlobalStaticMembers.apiSelected]
        )

        val internalStorageDir = application.filesDir
        val fileName = "userInfo.txt"
        val file = File(internalStorageDir, fileName)
        if (file.exists()) {
            file.delete()
        }
        try {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.IO) {
                Toast.makeText(this@CacheActivity, "写入用户信息失败！", Toast.LENGTH_LONG).show()
            }
            finish()
        }
        file.writeText(Gson().toJson(userInfo))
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}