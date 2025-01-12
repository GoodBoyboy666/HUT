package top.goodboyboy.hut.mainFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.google.gson.Gson
import top.goodboyboy.hut.Activity.BrowseActivity
import top.goodboyboy.hut.Activity.MainActivityPage
import top.goodboyboy.hut.R
import top.goodboyboy.hut.SettingsClass
import top.goodboyboy.hut.databinding.FragmentToolBinding
import top.goodboyboy.hut.mainFragment.FragmentHutServiceCenter.FragmentHutServiceCenter
import top.goodboyboy.hut.mainFragment.FragmentLoginHutApp.FragmentLoginHutApp
import java.io.File

class FragmentTool : Fragment() {
    private var _binding: FragmentToolBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.test.setOnClickListener{
//            val intent=Intent(requireContext(),BrowseActivity::class.java)
//            intent.putExtra("url","https://www.goodboyboy.top")
//            intent.putExtra("jwt","")
//            startActivity(intent)
//        }


        val internalStorageDir = requireContext().filesDir
        val fragmentManager = requireActivity().supportFragmentManager

        val fileName = "settings.txt"
        val file = File(internalStorageDir, fileName)

        var findAccessToken=false
        if (file.exists()) {
            val fileText = file.readText()
            if (fileText != "") {
                val settings =
                    Gson().fromJson(file.readText(), SettingsClass::class.java)
                if(settings.accessToken.isNotBlank()){
                    findAccessToken=true
                    fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragmentContainer,FragmentHutServiceCenter())
                        .commit()
                }
            }
        }

        if(!findAccessToken) {
            fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragmentContainer, FragmentLoginHutApp())
                .commit()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}