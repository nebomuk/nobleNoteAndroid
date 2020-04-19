package com.taiko.noblenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.taiko.noblenote.databinding.FragmentFindInFilesBinding

class FindInFilesFragment : Fragment()
{
    private lateinit var binding: FragmentFindInFilesBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentFindInFilesBinding.inflate(inflater,container,false);

        binding.lifecycleOwner = this;


        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory(requireActivity().application))
                .get(MainViewModel::class.java);

        val adapter = FindInFilesAdapter();

        mainViewModel.findInFilesResults.observe(this, Observer { adapter.update(it.toList()) })

        binding.recyclerView.adapter = adapter;
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())

    }
}