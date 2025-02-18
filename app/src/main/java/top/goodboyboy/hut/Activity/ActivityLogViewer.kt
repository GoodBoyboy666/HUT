package top.goodboyboy.hut.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import top.goodboyboy.hut.R

class ActivityLogViewer : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_viewer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val logSummary = intent.getStringExtra("log_summary")
        if (!logSummary.isNullOrEmpty()) {
            val textLog: TextView = findViewById(R.id.text_log)
            textLog.movementMethod = ScrollingMovementMethod()
            textLog.text = "提供反馈时请将下列信息完整提交给开发人员！！！\n\n${logSummary}"
        }
    }
}