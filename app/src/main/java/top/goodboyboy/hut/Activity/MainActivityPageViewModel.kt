package top.goodboyboy.hut.Activity

import androidx.lifecycle.ViewModel

class MainActivityPageViewModel : ViewModel() {
    //HutServiceCenter
    var serviceListString = ""

    //FragmentKb
    var allItems = List(40) { "" }.toMutableList()
    var allInfos = List(40) { "N/A" }.toMutableList()
    var isLoad = false
    var zhouciSelectedIndex = 0
    var zhouciSelected = String()
    var zhouci = mutableListOf<String>()
    var kbjcmsid = String()
    var xnxq01id = String()

}