package top.goodboyboy.hut.mainFragment.FragmentHutServiceCenter

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.hut.Activity.MainActivityPageViewModel
import top.goodboyboy.hut.Adapter.ServiceListAdapter
import top.goodboyboy.hut.HutApiFunction
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.ServiceItem
import top.goodboyboy.hut.Util.SettingsUtil
import top.goodboyboy.hut.databinding.FragmentHutServiceCenterBinding
import java.io.File
import java.io.FileWriter

class FragmentHutServiceCenter : Fragment() {

    private var _binding: FragmentHutServiceCenterBinding? = null
    private val binding get() = _binding!!
    private var job: Job? = null

    private val viewModel: FragmentHutServiceCenterViewModel by viewModels()
    private val mainActivityPageViewModel: MainActivityPageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHutServiceCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val setting = SettingsUtil(requireContext())
        binding.progressRelativeLayout.visibility=View.VISIBLE
//        val internalStorageDir = requireContext().filesDir
//        val fragmentManager = requireActivity().supportFragmentManager

//        val fileName = "settings.txt"
//        val file = File(internalStorageDir, fileName)
//
//        var findAccessToken = false
//        if (file.exists()) {
//            val fileText = file.readText()
//            if (fileText != "") {
//                findAccessToken = true
//                val settings =
//                    Gson().fromJson(file.readText(), SettingsClass::class.java)
//
//                if(mainActivityPageViewModel.serviceList!="")
//                {
//                    showServiceListFromObject(
//                        mainActivityPageViewModel.serviceList,
//                        KbFunction.checkDarkMode(requireContext()),
//                        settings.accessToken
//                    )
//                }else {
//
//                    job = CoroutineScope(Dispatchers.IO).launch {
//                        try {
//                            val serviceList = HutApiFunction.getServiceList(settings.accessToken)
//                            if (serviceList.isOk && serviceList.serviceList != null) {
//                                // 显示Service
//                                withContext(Dispatchers.Main) {
//                                    mainActivityPageViewModel.serviceList = serviceList.serviceList
//                                    showServiceListFromObject(
//                                        serviceList.serviceList,
//                                        KbFunction.checkDarkMode(requireContext()),
//                                        settings.accessToken
//                                    )
//                                }
//                            } else {
//                                withContext(Dispatchers.Main) {
//                                    Toast.makeText(
//                                        requireContext(),
//                                        serviceList.errorMessage,
//                                        Toast.LENGTH_SHORT
//                                    )
//                                        .show()
//
//                                    withContext(Dispatchers.IO) {
//                                        settings.accessToken = ""
//                                        val writer = FileWriter(file, false)
//                                        writer.write(Gson().toJson(settings))
//                                        writer.close()
//                                    }
//                                    fragmentManager.beginTransaction()
//                                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//                                        .replace(R.id.fragmentContainer, FragmentTool())
//                                        .commit()
//                                }
//                            }
//                        } catch (e: Exception) {
//                            withContext(Dispatchers.Main) {
//                                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
//                                    .show()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (!findAccessToken) {
//            Toast.makeText(requireContext(), "未找到配置文件", Toast.LENGTH_SHORT).show()
//            fragmentManager.beginTransaction()
//                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//                .replace(R.id.fragmentContainer, FragmentTool())
//                .commit()
//        }

        val serviceItems = getServiceListFromFile()

        if (mainActivityPageViewModel.serviceListString != "") {
            showServiceListFromString(
                mainActivityPageViewModel.serviceListString,
                KbFunction.checkDarkMode(requireContext()),
                setting.globalSettings.accessToken
            )
            binding.progressRelativeLayout.visibility=View.GONE
        } else if (serviceItems != null) {
            showServiceListFromObject(
                serviceItems,
                KbFunction.checkDarkMode(requireContext()),
                setting.globalSettings.accessToken
            )
            binding.progressRelativeLayout.visibility=View.GONE
        } else {
            job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val serviceList =
                        HutApiFunction.getServiceList(setting.globalSettings.accessToken)
                    if (serviceList.isOk && serviceList.serviceList != null) {
                        // 显示Service
                        withContext(Dispatchers.Main) {
                            mainActivityPageViewModel.serviceListString = serviceList.serviceList
                            val serviceItemsObject=showServiceListFromString(
                                serviceList.serviceList,
                                KbFunction.checkDarkMode(requireContext()),
                                setting.globalSettings.accessToken
                            )
                            binding.progressRelativeLayout.visibility = View.GONE
                            CoroutineScope(Dispatchers.IO).launch {
                                cacheServiceList(serviceItemsObject)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                serviceList.errorMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                binding.progressRelativeLayout.visibility=View.GONE
            }
        }

        binding.serviceCenterSwipeRefreshLayout.setOnRefreshListener {
            job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val serviceList =
                        HutApiFunction.getServiceList(setting.globalSettings.accessToken)
                    if (serviceList.isOk && serviceList.serviceList != null) {
                        withContext(Dispatchers.Main) {
                            mainActivityPageViewModel.serviceListString = serviceList.serviceList
                            val serviceItemsObject= showServiceListFromString(
                                serviceList.serviceList,
                                KbFunction.checkDarkMode(requireContext()),
                                setting.globalSettings.accessToken
                            )
                            binding.serviceCenterSwipeRefreshLayout.isRefreshing=false
                            CoroutineScope(Dispatchers.IO).launch {
                                cacheServiceList(serviceItemsObject)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                serviceList.errorMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }

    }

    private fun showServiceListFromString(stringJson: String, isDark: Boolean, jwt: String):MutableList<ServiceItem> {
        val jsonObject = JsonParser.parseString(stringJson)
        val serviceItems = mutableListOf<ServiceItem>()
        for (classify in jsonObject.asJsonObject.get("data").asJsonArray) {
            for (item in classify.asJsonObject.get("services").asJsonArray) {
                val serviceItem = ServiceItem(
                    item.asJsonObject.get("iconUrl").asString,
                    item.asJsonObject.get("serviceName").asString,
                    item.asJsonObject.get("serviceUrl").asString,
                    item.asJsonObject.get("tokenAccept").asString
                )
                serviceItems.add(serviceItem)
            }

        }
        binding.serviceGridview.adapter =
            ServiceListAdapter(requireContext(), serviceItems, isDark, jwt)
        return serviceItems
    }

    private fun showServiceListFromObject(
        serviceList: MutableList<ServiceItem>,
        isDark: Boolean,
        jwt: String
    ) {
        binding.serviceGridview.adapter =
            ServiceListAdapter(requireContext(), serviceList, isDark, jwt)
    }

    private fun cacheServiceList(serviceItems: MutableList<ServiceItem>) {
        val json = Gson().toJson(serviceItems)
        val fileName = "ServiceList.txt"
        val internalStorageDir = requireContext().filesDir
        val writer = FileWriter(File(internalStorageDir, fileName))
        writer.write(json)
        writer.close()
    }

    private fun getServiceListFromFile(): MutableList<ServiceItem>? {
        val fileName = "ServiceList.txt"
        val internalStorageDir = requireContext().filesDir
        val file = File(internalStorageDir, fileName)
        return if (file.exists()) {
            try {
                Gson().fromJson<MutableList<ServiceItem>>(
                    file.readText(),
                    object : TypeToken<MutableList<ServiceItem>>() {}.type
                )
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}