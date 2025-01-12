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
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.goodboyboy.hut.Activity.MainActivityPageViewModel
import top.goodboyboy.hut.Adapter.ServiceListAdapter
import top.goodboyboy.hut.HutApiFunction
import top.goodboyboy.hut.KbFunction
import top.goodboyboy.hut.R
import top.goodboyboy.hut.ServiceItem
import top.goodboyboy.hut.SettingsClass
import top.goodboyboy.hut.databinding.FragmentHutServiceCenterBinding
import top.goodboyboy.hut.databinding.FragmentLoginHutAppBinding
import top.goodboyboy.hut.mainFragment.FragmentLoginHutApp.FragmentLoginHutApp
import top.goodboyboy.hut.mainFragment.FragmentTool
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
        val internalStorageDir = requireContext().filesDir
        val fragmentManager = requireActivity().supportFragmentManager

        val fileName = "settings.txt"
        val file = File(internalStorageDir, fileName)

        var findAccessToken = false
        if (file.exists()) {
            val fileText = file.readText()
            if (fileText != "") {
                findAccessToken = true
                val settings =
                    Gson().fromJson(file.readText(), SettingsClass::class.java)

                if(mainActivityPageViewModel.serviceList!="")
                {
                    showServiceList(
                        mainActivityPageViewModel.serviceList,
                        KbFunction.checkDarkMode(requireContext()),
                        settings.accessToken
                    )
                }else {

                    job = CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val serviceList = HutApiFunction.getServiceList(settings.accessToken)
                            if (serviceList.isOk && serviceList.serviceList != null) {
                                // 显示Service
                                withContext(Dispatchers.Main) {
                                    mainActivityPageViewModel.serviceList = serviceList.serviceList
                                    showServiceList(
                                        serviceList.serviceList,
                                        KbFunction.checkDarkMode(requireContext()),
                                        settings.accessToken
                                    )
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireContext(),
                                        serviceList.errorMessage,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                                    withContext(Dispatchers.IO) {
                                        settings.accessToken = ""
                                        val writer = FileWriter(file, false)
                                        writer.write(Gson().toJson(settings))
                                        writer.close()
                                    }
                                    fragmentManager.beginTransaction()
                                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                        .replace(R.id.fragmentContainer, FragmentTool())
                                        .commit()
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
        }
        if (!findAccessToken) {
            Toast.makeText(requireContext(), "未找到配置文件", Toast.LENGTH_SHORT).show()
            fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragmentContainer, FragmentTool())
                .commit()
        }

    }

    private fun showServiceList(stringJson: String, isDark: Boolean, jwt: String) {
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
        binding.progressRelativeLayout.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}