package top.goodboyboy.hut

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.SupervisorJob
import top.goodboyboy.hut.databinding.FragmentKbBinding

class FragmentKb : Fragment() {

    private var _binding: FragmentKbBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewmodel: FragmentKbViewModel

    private lateinit var adapter: KbAdapter
    private lateinit var spinner: Spinner


    private val job = SupervisorJob()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKbBinding.inflate(inflater, container, false)
        viewmodel = ViewModelProvider(this).get(FragmentKbViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val isDarkMode = KbFunction.checkDarkMode(requireContext())

        //初始化课表View布局
        val gridView = binding.kbGrid
        val gridAdapterItems = GridAdapterItems(viewmodel.allitems, viewmodel.allinfos)
        adapter = KbAdapter(requireContext(), gridAdapterItems, isDarkMode)
        gridView.adapter = adapter


        //初始化用户与周次选择布局
//        if(viewmodel.mainFunction.isAuth)
//        {
//            binding.zhouciLinearLayout.visibility=View.VISIBLE
//        }
//        else
//        {
//            binding.zhouciLinearLayout.visibility=View.GONE
//        }

        //初始化Spinner
        spinner = binding.zhouciSpinner

        val spinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, viewmodel.zhouci)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.setSelection(viewmodel.zhouciSelectedIndex)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position) as String
                viewmodel.zhouciSelected = selectedItem
                viewmodel.zhouciSelectedIndex = position
//                loadKb(userNum, userPasswd)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        //初始化Header
        val gridHeaderView = binding.kbHead
        val gridHeaderAdapterItems = GridHeaderAdapterItems(viewmodel.kbHead)
        val headerAdapter = KbHeaderAdapter(requireContext(), gridHeaderAdapterItems, isDarkMode)
        gridHeaderView.adapter = headerAdapter


        binding.swipeRefreshLayout.setOnRefreshListener {
            loadKb()
        }

        if (viewmodel.isFirst) {
            viewmodel.isFirst = false
            loadKb()
        }
    }


    /**
     * 加载课表
     *
     */
    private fun loadKb() {

        binding.swipeRefreshLayout.isRefreshing = true


//        kbitems = viewmodel.mainFunction.getKbData(userNum,userPasswd)

        val internalStorageDir = requireContext().filesDir
        val fileName = Hash.hash(
            viewmodel.zhouciSelected +
                    viewmodel.kbjcmsid +
                    viewmodel.xnxq01id
        )

        val kbitems = KbFunction.getKbFromFile(
            internalStorageDir.path + "/kbs/" + fileName
        )

        flushUI(kbitems)


    }

    /**
     * 刷新课表
     *
     * @param kbitems kbItemsAsList对象
     */
    private fun flushUI(kbitems: KbItemsAsList) {
        if (kbitems.isOk) {

            viewmodel.allitems.clear()
            viewmodel.allinfos.clear()

//            for (item in kbitems.kbitems!!) {
//                for (kb in item) {
//                    viewmodel.allitems.add(kb.title)
//                }
//            }
//
//            for (item in kbitems.kbitems!!) {
//                for (kb in item) {
//                    viewmodel.allinfos.add(kb.infos)
//                }
//            }

            for (item in kbitems.kbitems!!) {
                viewmodel.allitems.add(item.title)
                viewmodel.allinfos.add(item.infos)
            }


            val spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                viewmodel.zhouci
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter
            spinner.setSelection(viewmodel.zhouciSelectedIndex)
            spinnerAdapter.notifyDataSetChanged()


            adapter.notifyDataSetChanged()

            //                Log.d("SpinnerData", "数据源内容: ${MainFunction.zhouci.joinToString()}\n" +
            //                        "适配器已设置，数据项数: ${spinnerAdapter.count}"+
            //                        "id${MainFunction.kbjcmsid}")

//            binding.zhouciLinearLayout.visibility = View.VISIBLE
        } else {
            Toast.makeText(requireContext(), kbitems.reason ?: "未知错误！", Toast.LENGTH_LONG)
                .show()
        }

        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.swipeRefreshLayout.isRefreshing = false
        job.cancel()
        _binding = null
    }
}