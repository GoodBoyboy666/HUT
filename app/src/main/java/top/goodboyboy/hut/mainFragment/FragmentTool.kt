package top.goodboyboy.hut.mainFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.SettingsUtil
import top.goodboyboy.hut.databinding.FragmentToolBinding
import top.goodboyboy.hut.mainFragment.FragmentHutServiceCenter.FragmentHutServiceCenter
import top.goodboyboy.hut.mainFragment.FragmentLoginHutApp.FragmentLoginHutApp

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

        val setting = SettingsUtil(requireContext())
        val fragmentManager = requireActivity().supportFragmentManager

        if (setting.globalSettings.accessToken.isNotBlank()) {
            fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragmentContainer, FragmentHutServiceCenter())
                .commit()
        } else {
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