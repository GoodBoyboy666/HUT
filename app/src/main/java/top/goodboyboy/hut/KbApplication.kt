package top.goodboyboy.hut

import android.app.Application
import top.goodboyboy.hut.others.UncaughtException

class KbApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        UncaughtException.getInstance(this)
    }
}