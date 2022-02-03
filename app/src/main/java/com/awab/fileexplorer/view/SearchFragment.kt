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
import com.awab.fileexplorer.adapters.SearchAdapter
import com.awab.fileexplorer.databinding.FragmentSearchBinding
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.utils.SEARCH_FOLDER_ARGS
import com.awab.fileexplorer.presenter.SearchFragmentPresenter
import com.awab.fileexplorer.presenter.contract.SearchPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.view.contract.ISearchFragmentView
import com.awab.fileexplorer.view.contract.StorageView

class SearchFragment : Fragment(), ISearchFragmentView {

    private val TAG = "SearchFragment"



    private lateinit var mSearchFragmentPresenter: SearchPresenterContract
    private lateinit var mMainPresenter: StoragePresenterContract

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private  var _adapter: SearchAdapter? = null
    private  val adapter: SearchAdapter
        get() = _adapter!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is StorageView){
            mMainPresenter = context.presenter
            val folderName = arguments?.getString(SEARCH_FOLDER_ARGS)!!
            mSearchFragmentPresenter = SearchFragmentPresenter(this, folderName, mMainPresenter)
        }
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

        _adapter = SearchAdapter().apply {
            setContext(requireContext())
            setPresenter(mSearchFragmentPresenter)
            setItemsList(listOf())
        }

        binding.rvSearchResults.adapter = adapter
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())

        binding.etSearchText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                mSearchFragmentPresenter.onTextChanged(s.toString())
            }
        })

        // bind this search presenter to tha main presenter
        mMainPresenter.searchPresenter = mSearchFragmentPresenter


        //  showing a progress bar until the Presenter is ready
        binding.searchProgressBar.visibility = View.VISIBLE

        // querying the files for search
        mSearchFragmentPresenter.loadFiles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // the adapter has the context... and it must be destroyed here(memory leak)
        _adapter = null
    }

    override fun searchTextEmpty() {
        adapter.setItemsList(listOf())
        binding.searchListLayout.visibility = View.VISIBLE
        binding.tvSearchResultsSize.text = ""
    }

    override fun searchResultEmpty() {
        binding.searchListLayout.visibility = View.GONE
    }

    override fun showSearchList(list: List<FileModel>, searchText: String) {
        adapter.setItemsList(list, searchText)
        binding.searchListLayout.visibility = View.VISIBLE
        binding.tvSearchResultsSize.text = "found <${list.size}>"
    }

    override fun isReady() {
        //  the controller is ready, updating the view to start searching
        binding.searchProgressBar.visibility = View.GONE
        binding.etSearchText.visibility = View.VISIBLE

        //  focusing on the Edit text
        binding.etSearchText.requestFocus()

        // showing the keyboard
        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchText, 0)

    }

    override fun removeInputMethod() {
        //  hiding the keyBoard

        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken,0)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        // reloading the list after config change
        val text = binding.etSearchText.text.toString()
        mSearchFragmentPresenter.onTextChanged(text)
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