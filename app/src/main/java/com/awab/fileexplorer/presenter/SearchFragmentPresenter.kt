package com.awab.fileexplorer.presenter

import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.utils.getSearchResults
import com.awab.fileexplorer.presenter.contract.SearchPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.presenter.threads.SearchListAsyncTask
import com.awab.fileexplorer.view.contract.ISearchFragmentView

class SearchFragmentPresenter(
    override val view: ISearchFragmentView,
    private val folderPath: String,
    private val mStoragePresenter: StoragePresenterContract
) : SearchPresenterContract {

    override val mainStoragePresenter: StoragePresenterContract
        get() = mStoragePresenter

    override var searchList: List<FileModel> = listOf()

    override fun onTextChanged(text: String) {
        if (text.isBlank())
            view.searchTextEmpty()
        else {
            val searchResults = getSearchResults(searchList, text)
            updateViewList(searchResults, text)
        }
    }

    private fun updateViewList(list: List<FileModel>, text: String) {
        if (list.isEmpty())
            view.searchResultEmpty()
        else
            view.showSearchList(list, text)
    }

    override fun isReady(list: List<FileModel>) {
        searchList = list
        view.isReady()
    }

    override fun loadFiles() {
        if (searchList.isEmpty())
            SearchListAsyncTask(this, folderPath, view.context().contentResolver).execute()
        else
            isReady(searchList)
    }

    override fun onFileClick(file: FileModel) {
        view.removeInputMethod()
        mainStoragePresenter.onFileClickedFromSearch(file)
    }

    override fun onFileLongClick(file: FileModel) {
    }

    override fun selectOrUnClickedItem(file: FileModel) {
    }

    override fun selectAll() {
    }

    override fun getSelectedItems(): List<FileModel> {
        return listOf()
    }

    override fun getSelectedItemCount(): Int {
        return 0
    }

    override fun stopActionMode() {
    }
}