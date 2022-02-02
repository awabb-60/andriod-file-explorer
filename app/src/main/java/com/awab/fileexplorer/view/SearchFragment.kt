package com.awab.fileexplorer.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.adapters.SearchAdapter
import com.awab.fileexplorer.presenter.SearchFragmentPresenter
import com.awab.fileexplorer.databinding.FragmentSearchBinding
import com.awab.fileexplorer.model.utils.SEARCH_FOLDER_ARGS
import com.awab.fileexplorer.view.contract.ISearchFragmentView

class SearchFragment : Fragment(), ISearchFragmentView {

    private val TAG = "SearchFragment"
    private lateinit var adapter: SearchAdapter
    private lateinit var mSearchFragmentController: SearchFragmentPresenter

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        showing a progress bar until the controller is ready
        binding.searchProgressBar.visibility = View.VISIBLE

        adapter = SearchAdapter().apply {
            setContextAndListener(context as Context)
            setItemsList(listOf())
        }

        binding.rvSearchResults.adapter = adapter
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())

        binding.etSearchText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                mSearchFragmentController.searchTextChanged(s.toString())
            }
        })

//      this must to be after instantiation of the binding
        val folderName = arguments?.getString(SEARCH_FOLDER_ARGS)!!
        mSearchFragmentController = SearchFragmentPresenter(this, folderName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showSearchList(list: List<FileModel>, searchText: String) {
        adapter.setItemsList(list, searchText)
        binding.searchListLayout.visibility = View.VISIBLE
        binding.tvSearchResultsSize.text = "found <${list.size}>"
    }

    override fun searchResultEmpty() {
        binding.searchListLayout.visibility = View.GONE
    }

    override fun searchTextEmpty() {
        adapter.setItemsList(listOf())
        binding.searchListLayout.visibility = View.VISIBLE
        binding.tvSearchResultsSize.text = ""
    }

    override fun isReady() {
//        the controller is ready, updating the view to start searching
        binding.searchProgressBar.post {
            binding.searchProgressBar.visibility = View.GONE
            binding.etSearchText.visibility = View.VISIBLE
//            focusing on the Edit text
            binding.etSearchText.requestFocus()

            // showing the keyboard
            val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSearchText,0)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val text = binding.etSearchText.text.toString()
        mSearchFragmentController.searchTextChanged(text)
    }

    companion object {
        fun newInstance(folderPath: String): SearchFragment {
            val fragment = SearchFragment()

            val args = Bundle().apply {
                putString(SEARCH_FOLDER_ARGS, folderPath)
            }
            fragment.arguments = args
            return fragment
        }
    }
}