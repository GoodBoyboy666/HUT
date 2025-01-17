package top.goodboyboy.hut.Activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import top.goodboyboy.hut.HutApiFunction
import top.goodboyboy.hut.R
import top.goodboyboy.hut.TokenTypeName
import top.goodboyboy.hut.databinding.ActivityBrowseBinding
import top.goodboyboy.hut.others.UncaughtException

class BrowseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBrowseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //全局捕捉异常
        UncaughtException.getInstance(this)

        var url = intent.getStringExtra("url") ?: ""
        val jwt = intent.getStringExtra("jwt")
        val tokenAccept = intent.getStringExtra("tokenAccept") ?: ""

        val tokenTypeName = HutApiFunction.parseTokenType(tokenAccept)


        val webView = binding.browseWebView
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.getSettings().setDomStorageEnabled(true);

        webView.webViewClient =object :WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                if(url!=null){
                    if (isExternalLink(url)) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            view?.context?.startActivity(intent)
                            return true
                        } catch (e: ActivityNotFoundException) {
                            // 处理异常
                            Toast.makeText(this@BrowseActivity, "无法调起应用，请检查是否安装了相关应用！", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                return false
            }
            private fun isExternalLink(url: String): Boolean {
                return url.startsWith("weixin") || url.startsWith("bankabc")
            }
        }
//        webView.webViewClient = object : WebViewClient() {
//            ///only for test
//            override fun onReceivedSslError(
//                view: WebView?,
//                handler: SslErrorHandler?,
//                error: android.net.http.SslError?
//            ) {
//                // 忽略 SSL 证书错误
//                handler?.proceed()
//            }
//
//            override fun onReceivedError(
//                view: WebView?,
//                request: WebResourceRequest?,
//                error: WebResourceError?
//            ) {
//                super.onReceivedError(view, request, error)
//                // 处理其他错误
//            }
//
//            ///
//        }

        if (tokenTypeName.urlTokenKeyName != "") {
            val uriObj = Uri.parse(url)
            val builder = uriObj.buildUpon()
            builder.appendQueryParameter(tokenTypeName.urlTokenKeyName, jwt)
            url = builder.build().toString()
        }

        if (tokenTypeName.headerTokenKeyName != "") {
            val header = mapOf(
                tokenTypeName.headerTokenKeyName to jwt,
            )
            webView.loadUrl(url, header)
        } else {
            webView.loadUrl(url)
        }


        //设置返回事件
        val backEvent = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.browseWebView.canGoBack()) {
                    binding.browseWebView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backEvent)

        binding.backImageButton.setOnClickListener {
            if (binding.browseWebView.canGoBack()) {
                binding.browseWebView.goBack()
            } else {
                finish()
            }
        }
        binding.closeImageButton.setOnClickListener {
            finish()
        }
    }
}