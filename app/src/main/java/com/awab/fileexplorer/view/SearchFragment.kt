package com.awab.fileexplorer.view

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.FragmentSearchBinding
import com.awab.fileexplorer.presenter.SearchFragmentPresenter
import com.awab.fileexplorer.presenter.contract.SearchPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.utils.SEARCH_STORAGE_PATH_ARGS
import com.awab.fileexplorer.utils.adapters.SearchAdapter
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.view.contract.ISearchFragmentView
import com.awab.fileexplorer.view.contract.StorageView

class SearchFragment : Fragment(), ISearchFragmentView {

    private val TAG = "SearchFragment"

    private lateinit var mSearchFragmentPresenter: SearchPresenterContract
    private lateinit var mMainPresenter: StoragePresenterContract

    private lateinit var storagePath: String

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var _adapter: SearchAdapter? = null
    private val adapter: SearchAdapter
        get() = _adapter!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is StorageView) {
            mMainPresenter = context.presenter

            storagePath = arguments?.getString(SEARCH_STORAGE_PATH_ARGS)!!
            mSearchFragmentPresenter = SearchFragmentPresenter(this, storagePath, mMainPresenter)
        }
    }

    override fun context(): Context {
        return requireContext()
    }

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

        _adapter = SearchAdapter(requireContext(), mSearchFragmentPresenter)

        binding.rvSearchResults.adapter = adapter
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                removeInputMethod()
                if (query != null)
                    mSearchFragmentPresenter.onTextChanged(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null)
                    mSearchFragmentPresenter.onTextChanged(newText)
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            removeInputMethod()
            false
        }

        binding.searchView.setIconifiedByDefault(false)
        binding.searchView.queryHint = getString(R.string.search_hint)

        // bind this search presenter to tha main presenter
        mMainPresenter.bindSupPresenter(mSearchFragmentPresenter)

        //  showing a progress bar until the presenter is ready with the search list
        binding.searchProgressBar.visibility = View.VISIBLE

        // querying the search list
        mSearchFragmentPresenter.loadFiles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mSearchFragmentPresenter.cancelLoadFiles()
        _binding = null
        // the adapter has the context... and it must be destroyed here(memory leak)
        _adapter = null
    }

    override fun searchTextEmpty() {
        adapter.setItemsList(listOf())
        binding.searchListLayout.visibility = View.VISIBLE
        binding.emptyFolderLayout.visibility = View.GONE
        binding.tvSearchResultsSize.text = ""
    }

    override fun searchResultEmpty() {
        binding.searchListLayout.visibility = View.GONE
        binding.emptyFolderLayout.visibility = View.VISIBLE
    }

    override fun showSearchList(list: List<FileDataModel>, searchText: String) {
        adapter.setItemsList(list, searchText)
        binding.searchListLayout.visibility = View.VISIBLE
        binding.emptyFolderLayout.visibility = View.GONE
        binding.tvSearchResultsSize.text = "found <${list.size}>"
    }

    override fun isReady() {
        //  the presenter is ready, updating the view to start searching
        binding.searchProgressBar.visibility = View.GONE
        binding.searchView.visibility = View.VISIBLE

        //  focusing on the Edit text
        binding.searchView.requestFocus()

        // showing the keyboard
        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun removeInputMethod() {
        //  hiding the keyBoard
        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun selectOrUnSelect(file: FileDataModel) {
        adapter.selectOrUnSelect(file)
    }

    override fun selectAll() {
        adapter.selectAll()
    }

    override fun getAllItems(): List<FileDataModel> {
        return adapter.getAllItems()
    }

    override fun getSelectedItems(): List<FileDataModel> {
        return adapter.getSelectedItems()
    }

    override fun stopActionMode() {
        adapter.stopActionMode()
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // reloading the list after config change
        val text = binding.searchView.query
        mSearchFragmentPresenter.onTextChanged(text.toString())
    }

    override fun finishFragment() {
        activity?.supportFragmentManager?.popBackStack()
    }

    companion object {
        fun newInstance(storagePath: String): SearchFragment {
            val fragment = SearchFragment()

            val args = Bundle().apply {
                putString(SEARCH_STORAGE_PATH_ARGS, storagePath)
            }
            fragment.arguments = args
            return fragment
        }
    }
}