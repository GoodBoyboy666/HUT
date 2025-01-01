package top.goodboyboy.hut.mainFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import top.goodboyboy.hut.Activity.BrowseActivity
import top.goodboyboy.hut.databinding.FragmentToolBinding

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
        binding.test.setOnClickListener{
            val intent=Intent(requireContext(),BrowseActivity::class.java)
            intent.putExtra("url","https://www.goodboyboy.top")
            intent.putExtra("jwt","")
            startActivity(intent)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}