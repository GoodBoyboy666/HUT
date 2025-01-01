package top.goodboyboy.hut.Activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import top.goodboyboy.hut.GlobalStaticMembers
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.R
import top.goodboyboy.hut.mainFragment.FragmentKb.FragmentKb
import top.goodboyboy.hut.databinding.ActivityMainPageBinding
import top.goodboyboy.hut.mainFragment.FragmentMe
import top.goodboyboy.hut.mainFragment.FragmentTool
import top.goodboyboy.hut.others.UncaughtException

class MainActivityPage : AppCompatActivity() {
    private lateinit var binding: ActivityMainPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        UncaughtException.getInstance(this)


        //初始化主题颜色
        val isDarkMode = KbFunction.checkDarkMode(this)

        var hutNavBarColor = R.drawable.hut_nav_bar
        var bottomBackground = R.drawable.bottom_background

        if (isDarkMode) {
            binding.mainPage.setBackgroundResource(R.color.black)
            hutNavBarColor = R.color.grey
            bottomBackground = R.color.grey
        }

        //初始化toolbar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        val toolbarDrawable = ContextCompat.getDrawable(this, hutNavBarColor)
        toolbar.background = toolbarDrawable

        //初始化底部导航栏
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val bottomNavigationViewDrawable = ContextCompat.getDrawable(this, bottomBackground)
        bottomNavigationView.background = bottomNavigationViewDrawable

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_item1 -> {
                    replaceFragment(FragmentKb())
                    true
                }

                R.id.navigation_item2 -> {
                    replaceFragment(FragmentTool())
                    true
                }

                R.id.navigation_item3 -> {
                    replaceFragment(FragmentMe())
                    true
                }

                else -> false
            }
        }
        replaceFragment(FragmentKb())
    }

    private fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        return when (item.itemId) {
            R.id.action_about -> {

                showAlertDialog(
                    this,
                    getString(R.string.about),
                    getString(R.string.about_info, GlobalStaticMembers.VersionName),
                    isDarkMode
                )

                true
            }

            R.id.action_author -> {
                val url = "https://www.goodboyboy.top"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                true
            }

            R.id.action_thanks -> {
                val intent = Intent(this, Thanks::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    companion object {
        /**
         * 显示提示框
         *
         * @param context 上下文
         * @param title 标题
         * @param message 内容
         * @param isDark 是否暗色模式
         */
        fun showAlertDialog(context: Context, title: String, message: String, isDark: Boolean) {
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val dialogView: View = inflater.inflate(R.layout.custom_dialog, null)
            val dialog: LinearLayout = dialogView.findViewById(R.id.custom_alertdialog)
            val titleTextView: TextView = dialogView.findViewById(R.id.dialog_title)
            val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
            val positiveButton: Button = dialogView.findViewById(R.id.dialog_positiveButton)
            var dialogBackground = R.drawable.info_border
            var okBackground = R.drawable.kb_ok
            titleTextView.text = title
            messageTextView.text = message

            if (isDark) {
                dialogBackground = R.color.grey
                okBackground = R.color.grey
            }
            dialog.setBackgroundResource(dialogBackground)
            positiveButton.setBackgroundResource(okBackground)
            builder.setView(dialogView)
            val alertDialog = builder.create()
            positiveButton.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
    }


}