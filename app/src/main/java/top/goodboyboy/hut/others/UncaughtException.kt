package top.goodboyboy.hut.others

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import top.goodboyboy.hut.Activity.ActivityLogViewer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

//来自https://www.jianshu.com/p/0ea4615674f0
//本文件内容版权归https://www.jianshu.com/p/0ea4615674f0所有

class UncaughtException private constructor(context: Context):Thread.UncaughtExceptionHandler {
    // 上下文对象
    private val mContext = context

    // 用于存放参数信息
    private val info = LinkedHashMap<String, String>()

    // 用于格式化日期
    private val mDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // 单例模式
    companion object {
        private var instance: UncaughtException? = null
        fun getInstance(context: Context): UncaughtException? {
            if (instance == null) {
                synchronized(UncaughtException::class) {
                    instance = UncaughtException(context)
                }
            }
            return instance
        }
    }

    /**
     * 构造初始化
     */
    init {
        // 设置当前类为应用默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当 UncaughtException 发生时转入该函数
     * @param t
     * @param e
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        handleException(e)
        try {
//            Thread.sleep(2000)
        } catch (e: Exception) {
            Log.e("uncaughtException",e.message?:"Can't get error message")
        }
        exitProcess(0)
    }

    /**
     * 自定义错误处理
     * @param e
     */
    private fun handleException(e: Throwable) {
        Thread {
            Looper.prepare()
            Toast.makeText(mContext, "出现非预期异常，正在收集日志！", Toast.LENGTH_SHORT).show()
            Looper.loop()
        }.start()

        // 跳转到日志显示界面
        val intent = Intent(mContext, ActivityLogViewer::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("log_summary", getLogSummary(e))
        mContext.startActivity(intent)

        /**
         * 这里可以执行一些业务操作
         * 比如保存崩溃日志到文件等等
         */
        Log.e("uncaughtException",e.message?:"Can't get error message")

    }

    /**
     * 收集参数信息
     * @param context
     */
    private fun putInfoToMap(context: Context) {
        info["设备型号"] = Build.MODEL
        info["设备品牌"] = Build.BOARD
        info["硬件名称"] = Build.HARDWARE
        info["硬件制造商"] = Build.MANUFACTURER
        info["系统版本"] = Build.VERSION.RELEASE
        info["系统版本号"] = "${Build.VERSION.SDK_INT}"

        val pm = context.packageManager
        val pi = pm.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        if (pi != null) {
            info["应用版本"] = pi.versionName
            info["应用版本号"] = "${PackageInfoCompat.getLongVersionCode(pi)}"
        }
    }

    /**
     * 获取日志头信息
     * @return StringBuffer
     */
    private fun getLogHeader(): StringBuffer {
        val sb = StringBuffer()
        sb.append(">>>>时间: ${mDateFormat.format(Date())}\n")
        putInfoToMap(mContext)
        info.entries.forEach {
            sb.append("${it.key}: ${it.value}\n")
        }
        return sb
    }

    /**
     * 获取日志概要
     * @param e
     * @return 日志概要
     */
    private fun getLogSummary(e: Throwable): String {
        val sb = getLogHeader().append("\n")
        sb.append("异常类: ${e.javaClass}\n")
        sb.append("异常信息: ${e.message}\n\n")
        for (i in e.stackTrace.indices) {
            sb.append("****堆栈追踪 ${i + 1}\n")
            sb.append("类名: ${e.stackTrace[i].className}\n")
            sb.append("方法: ${e.stackTrace[i].methodName}\n")
            sb.append("文件: ${e.stackTrace[i].fileName}\n")
            sb.append("行数: ${e.stackTrace[i].lineNumber}\n\n")
        }
        return sb.toString().trim()
    }
}