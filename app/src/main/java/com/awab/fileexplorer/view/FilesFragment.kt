package com.awab.fileexplorer.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.adapters.FilesAdapter
import com.awab.fileexplorer.databinding.FragmentFileBinding
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.utils.FILE_PATH_ARGS
import com.awab.fileexplorer.presenter.FilePresenter
import com.awab.fileexplorer.presenter.contract.FilesListPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.view.contract.IFileFragmentView
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File

class FilesFragment() : Fragment(), IFileFragmentView {
    private val TAG = "FilesFragment"

    private var _adapter: FilesAdapter? = null
    private val adapter: FilesAdapter
    get() = _adapter!!

    private lateinit var mFileFragmentPresenter: FilesListPresenterContract
    private lateinit var mMainPresenter: StoragePresenterContract

    private var _binding: FragmentFileBinding? = null
    private val binding
        get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is StorageView){
            mMainPresenter = context.presenter
        }

        val filePath = arguments?.getString(FILE_PATH_ARGS)!!
//        attaching this fragment view to the presenter
        mFileFragmentPresenter = FilePresenter(this, File(filePath), mMainPresenter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _adapter = FilesAdapter().apply {
            setContext(requireContext())
            setPresenter(mFileFragmentPresenter)
        }

        binding.rvFilesList.adapter = adapter
        binding.rvFilesList.layoutManager = LinearLayoutManager(context)
        binding.rvFilesList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.rvFilesList.setHasFixedSize(true)

//        bind this view to the main presenter
        mMainPresenter.filesListPresenter = mFileFragmentPresenter
        mFileFragmentPresenter.loadFiles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _adapter = null
    }

    override fun selectOrUnSelectItem(file: FileModel) {
        adapter.selectOrUnSelect(file)
    }

    override fun selectAll() {
        adapter.selectAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        mFileFragmentPresenter.removeBreadcrumb()
    }

    override fun context() = requireContext()

    override fun updateList(list: List<FileModel>) {
        updateFilesList(list)
    }

    private fun updateFilesList(list: List<FileModel>) {
        adapter.submitList(list)
//        hiding the rv to show the empty file indicator
        binding.rvFilesList.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    fun refreshList() {
        mFileFragmentPresenter.loadFiles()
    }

    override fun getSelectedItems(): List<FileModel> {
        return adapter.getSelectedItems()
    }

    override fun stopActionMode() {
        adapter.stopActionMode()
    }

    //    to create new instances from this fragment
    companion object {
        fun newInstance(filePath: String, storagePresenter: StoragePresenterContract): FilesFragment {
            val fragment = FilesFragment()

            val args = Bundle().apply {
                putString(FILE_PATH_ARGS, filePath)
            }
            fragment.arguments = args
            return fragment
        }
    }

}