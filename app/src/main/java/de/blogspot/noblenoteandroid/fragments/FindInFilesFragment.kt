package de.blogspot.noblenoteandroid.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.blogspot.noblenoteandroid.R
import de.blogspot.noblenoteandroid.adapters.FindInFilesAdapter
import de.blogspot.noblenoteandroid.databinding.FragmentFindInFilesBinding
import de.blogspot.noblenoteandroid.extensions.createNoteEditorArgs
import de.blogspot.noblenoteandroid.viewmodels.FindInFilesViewModel


class FindInFilesFragment : Fragment()
{
    private lateinit var binding: FragmentFindInFilesBinding
    private lateinit var findInFilesViewModel: FindInFilesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentFindInFilesBinding.inflate(inflater,container,false);

        binding.lifecycleOwner = viewLifecycleOwner;

        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findInFilesViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application))
                .get(FindInFilesViewModel::class.java);

        binding.viewModel = findInFilesViewModel;

        showKeyboard()

        binding.toolbar.toolbarFindInFiles.setNavigationOnClickListener {
            findNavController().navigateUp();
        }

        val adapter = FindInFilesAdapter();

        binding.recyclerView.adapter = adapter;
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerView.itemAnimator = null;

        findInFilesViewModel.startNoteEditor.observe(viewLifecycleOwner,
                Observer { findNavController().navigate(R.id.action_findInFilesFragment_to_editorFragment,
                        createNoteEditorArgs(
                                file = it.file,
                                argOpenMode = "",
                                argQueryText = if(it.line.isNotBlank()) findInFilesViewModel.toolbarFindInFilesText.value.orEmpty() else ""
                        )) })

    }

    private fun showKeyboard() {
        val editText = binding.toolbar.editText;
        editText.requestFocus()
        val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}