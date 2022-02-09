package com.awab.fileexplorer.presenter

import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.utils.SHARED_PREFERENCES_SHOW_HIDDEN_FILES
import com.awab.fileexplorer.model.utils.VIEW_SETTINGS_SHARED_PREFERENCES
import com.awab.fileexplorer.model.utils.getSearchResults
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
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

    private fun showHiddenFiles(): Boolean {
        val sp = view.context().getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, MODE_PRIVATE)
        return sp.getBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, false)
    }

    override fun isReady(list: List<FileModel>) {
        searchList = if (!showHiddenFiles()) // filtering the hidden files
            list.filter { !it.name.startsWith('.') }
        else
            list
        view.isReady()
    }

    override fun loadFiles() {
        if (searchList.isEmpty())
            SearchListAsyncTask(folderPath, view.context().contentResolver,
                object : SimpleSuccessAndFailureCallback<List<FileModel>> {
                    override fun onSuccess(data: List<FileModel>) {
                        isReady(data)
                    }

                    override fun onFailure(message: String) {
                        Toast.makeText(view.context(), message, Toast.LENGTH_SHORT)
                            .show()
                        view.finishFragmnet()
                    }
                }).execute()
        else
            isReady(searchList)
    }

    override fun onFileClick(file: FileModel) {
        view.removeInputMethod()
        mainStoragePresenter.onFileClicked(file)
    }

    override fun onFileLongClick(file: FileModel) {
        mainStoragePresenter.onFileLongClicked(file)
    }

    override fun selectOrUnSelectItem(file: FileModel) {
        view.selectOrUnSelect(file)
    }

    override fun selectAll() {
        view.selectAll()
    }

    override fun getSelectedItems(): List<FileModel> {
        return view.getSelectedItems()
    }

    override fun getSelectedItemCount(): Int {
        return getSelectedItems().count()
    }

    override fun stopActionMode() {
        view.stopActionMode()
    }
}