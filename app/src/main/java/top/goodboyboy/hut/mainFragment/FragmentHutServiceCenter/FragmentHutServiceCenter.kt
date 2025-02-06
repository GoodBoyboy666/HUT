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
        binding.progressRelativeLayout.visibility = View.VISIBLE

        val serviceItems = getServiceListFromFile()

        if (mainActivityPageViewModel.serviceListString != "") {
            showServiceListFromString(
                mainActivityPageViewModel.serviceListString,
                KbFunction.checkDarkMode(requireContext()),
                setting.globalSettings.accessToken
            )
            showFrequentServiceList(
                KbFunction.checkDarkMode(requireContext()),
                setting.globalSettings.accessToken
            )
            binding.progressRelativeLayout.visibility = View.GONE
        } else if (serviceItems != null) {
            showServiceListFromObject(
                serviceItems,
                KbFunction.checkDarkMode(requireContext()),
                setting.globalSettings.accessToken
            )
            showFrequentServiceList(
                KbFunction.checkDarkMode(requireContext()),
                setting.globalSettings.accessToken
            )
            binding.progressRelativeLayout.visibility = View.GONE
        } else {
            job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val serviceList =
                        HutApiFunction.getServiceList(setting.globalSettings.accessToken)
                    if (serviceList.isOk && serviceList.serviceList != null) {
                        // 显示Service
                        withContext(Dispatchers.Main) {
                            mainActivityPageViewModel.serviceListString = serviceList.serviceList
                            val serviceItemsObject = showServiceListFromString(
                                serviceList.serviceList,
                                KbFunction.checkDarkMode(requireContext()),
                                setting.globalSettings.accessToken
                            )
                            showFrequentServiceList(
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
                binding.progressRelativeLayout.visibility = View.GONE
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
                            val serviceItemsObject = showServiceListFromString(
                                serviceList.serviceList,
                                KbFunction.checkDarkMode(requireContext()),
                                setting.globalSettings.accessToken
                            )
                            binding.serviceCenterSwipeRefreshLayout.isRefreshing = false
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

    /**
     * 从字符串中显示服务列表
     *
     * @param stringJson json格式的字符串
     * @param isDark 暗色模式
     * @param jwt jwt
     * @return ServiceItem数组
     */
    private fun showServiceListFromString(
        stringJson: String,
        isDark: Boolean,
        jwt: String
    ): MutableList<ServiceItem> {
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

    /**
     * 从ServiceItem数组中显示服务列表
     *
     * @param serviceList ServiceItem数组
     * @param isDark 暗色模式
     * @param jwt jwt
     */
    private fun showServiceListFromObject(
        serviceList: MutableList<ServiceItem>,
        isDark: Boolean,
        jwt: String
    ) {
        binding.serviceGridview.adapter =
            ServiceListAdapter(requireContext(), serviceList, isDark, jwt)
    }

    /**
     * 显示常用服务列表
     *
     * @param isDark 暗色模式
     * @param jwt jwt
     */
    private fun showFrequentServiceList(
        isDark: Boolean,
        jwt: String
    ) {
        val serviceList = listOf(
            ServiceItem(
                "https://portal.hut.edu.cn/portal-minio/service/947钱包_1685355992559.png",
                "校园卡",
                "https://v8mobile.hut.edu.cn/homezzdx/openHomePage",
                "[{\"tokenType\":\"url\",\"tokenKey\":\"X-Id-Token\"}]"
            ),
            ServiceItem(
                "https://portal.hut.edu.cn/portal-minio/service/充值_1735025220964.png",
                "校园卡充值",
                "https://hub.17wanxiao.com/bsacs/light.action?flag=supwisdomapp_hngydxsw&ecardFunc=recharge",
                "[{\"tokenType\":\"url\",\"tokenKey\":\"token\"}]"
            ),
            ServiceItem(
                "https://portal.hut.edu.cn/portal-minio/service/9_26__1685350007771.png",
                "用水服务",
                "https://v8mobile.hut.edu.cn/zdRedirect/toSingleMenu?code=openWater",
                "[{\"tokenType\":\"header\",\"tokenKey\":\"X-Id-Token\"},{\"tokenType\":\"url\",\"tokenKey\":\"token\"}]"
            ),
            ServiceItem(
                "https://portal.hut.edu.cn/portal-minio/service/9_9__1685350006675.png",
                "电费充值",
                "https://v8mobile.hut.edu.cn/zdRedirect/toSingleMenu?code=openElePay",
                "[{\"tokenType\":\"url\",\"tokenKey\":\"X-Id-Token\"}]"
            ),
            ServiceItem(
                "https://portal.hut.edu.cn/portal-minio/service/教务_1__1733368821328.png",
                "教务综合服务",
                "https://mycas.hut.edu.cn/cas/login?service=https%3A%2F%2Fjwxtsj.hut.edu.cn%2Fnjwhd%2FloginSso",
                "[{\"tokenType\":\"url\",\"tokenKey\":\"token\"}]"
            )
        )
        binding.frequentlyServiceGridview.adapter =
            ServiceListAdapter(requireContext(), serviceList, isDark, jwt)
    }

    /**
     * 缓存服务列表
     *
     * @param serviceItems ServiceItem数组
     */
    private fun cacheServiceList(serviceItems: MutableList<ServiceItem>) {
        val json = Gson().toJson(serviceItems)
        val fileName = "ServiceList.txt"
        val internalStorageDir = requireContext().filesDir
        val writer = FileWriter(File(internalStorageDir, fileName))
        writer.write(json)
        writer.close()
    }

    /**
     * 从文件中加载ServiceItem数组
     *
     * @return ServiceItem数组
     */
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