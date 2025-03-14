package top.goodboyboy.hut.Activity

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.hut.CheckUpdate
import top.goodboyboy.hut.GlobalStaticMembers
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.AlertDialogUtil
import top.goodboyboy.hut.Util.SettingsUtil
import top.goodboyboy.hut.mainFragment.FragmentKb.FragmentKb
import top.goodboyboy.hut.databinding.ActivityMainPageBinding
import top.goodboyboy.hut.mainFragment.FragmentMe
import top.goodboyboy.hut.mainFragment.FragmentTool

class MainActivityPage : AppCompatActivity() {
    private lateinit var binding: ActivityMainPageBinding
    private var selectedFragmentId = R.id.navigation_item1
    private val viewModel: MainActivityPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        window.statusBarColor = Color.parseColor("#F2BA6D")

        //初始化主题颜色
        val isDarkMode = KbFunction.checkDarkMode(this)

        var hutNavBarColor = R.drawable.hut_nav_bar
        var bottomBackground = R.drawable.bottom_background

        if (isDarkMode) {
            binding.mainPage.setBackgroundResource(R.color.black)
            hutNavBarColor = R.color.grey
            bottomBackground = R.color.grey
        }

        val setting = SettingsUtil(this)

        //检测更新
        if (!setting.globalSettings.noMoreReminders) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val status = CheckUpdate.getLatestVersionFromGitea()
                    withContext(Dispatchers.Main) {
                        if (status.isSuccess && status.versionInfo?.verName != setting.globalSettings.ignoreVersion) {
                            val alert = AlertDialogUtil(
                                this@MainActivityPage,
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
                            }
                            alert.onClickIgnoreTheVersionButton = {
                                setting.globalSettings.ignoreVersion =
                                    status.versionInfo?.verName ?: "未知版本号"
                                setting.save()
                            }
                            alert.show()
                        }
                    }
                } catch (_: Exception) {

                }
            }
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
            selectedFragmentId = menuItem.itemId
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
        if (savedInstanceState != null) {
            selectedFragmentId =
                savedInstanceState.getInt("SELECTED_FRAGMENT_ID", R.id.navigation_item1)
        }

        bottomNavigationView.selectedItemId = selectedFragmentId
    }

    private fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SELECTED_FRAGMENT_ID", selectedFragmentId)
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

                AlertDialogUtil(
                    this,
                    getString(R.string.about),
                    getString(R.string.about_info, GlobalStaticMembers.VersionName),
                    isDarkMode
                ).show()

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
    }


}