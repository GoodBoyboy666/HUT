package top.goodboyboy.hut.Activity

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import top.goodboyboy.hut.R
import top.goodboyboy.hut.databinding.ActivityBrowseBinding
import top.goodboyboy.hut.others.UncaughtException

class BrowseActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBrowseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityBrowseBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        val url=intent.getStringExtra("url")?:""
        val jwt=intent.getStringExtra("jwt")

        //全局捕捉异常
        UncaughtException.getInstance(this)

        val webView=binding.browseWebView
        webView.settings.javaScriptEnabled=true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
    }
}