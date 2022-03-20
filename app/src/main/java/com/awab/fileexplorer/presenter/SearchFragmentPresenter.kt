package com.awab.fileexplorer.presenter

import android.provider.MediaStore
import android.widget.Toast
import com.awab.fileexplorer.model.MainStorageModel
import com.awab.fileexplorer.presenter.contract.SearchPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.getSearchResults
import com.awab.fileexplorer.view.contract.ISearchFragmentView

class SearchFragmentPresenter(
    override val view: ISearchFragmentView,
    private val folderPath: String,
    private val mStoragePresenter: StoragePresenterContract
) : SearchPresenterContract {

    override val mainStoragePresenter: StoragePresenterContract
        get() = mStoragePresenter

    override var searchList: List<FileDataModel> = listOf()

    val model = MainStorageModel(view.context())

    override fun onTextChanged(text: String) {
        if (text.isBlank())
            view.searchTextEmpty()
        else {
            val searchResults = getSearchResults(searchList, text)
            updateViewList(searchResults, text)
        }
    }

    private fun updateViewList(list: List<FileDataModel>, text: String) {
        if (list.isEmpty())
            view.searchResultEmpty()
        else
            view.showSearchList(list, text)
    }

    override fun isReady(list: List<FileDataModel>) {
        searchList = if (!model.viewHiddenFilesSettings()) // filtering the hidden files
            list.filter { !it.name.startsWith('.') }
        else
            list
        view.isReady()
    }

    override fun loadFiles() {
        if (searchList.isEmpty()) {

            val contentUri = MediaStore.Files.getContentUri("external")
            val selection = "_data Like ?"
            val selectionArgs = arrayOf("%$folderPath%")

            model.queryFiles(
                contentUri,
                null,
                selection,
                selectionArgs,
                object : SimpleSuccessAndFailureCallback<List<FileDataModel>> {
                    override fun onSuccess(data: List<FileDataModel>) {
                        isReady(data)
                    }

                    override fun onFailure(message: String) {
                        Toast.makeText(view.context(), message, Toast.LENGTH_SHORT)
                            .show()
                        view.finishFragment()
                    }
                })
        }
        else
            isReady(searchList)
    }

    override fun cancelLoadFiles() {
        model.cancelQueryFiles()
    }

    override fun onFileClick(file: FileDataModel) {
        view.removeInputMethod()
        mainStoragePresenter.onFileClicked(file)
    }

    override fun onFileLongClick(file: FileDataModel) {
        mainStoragePresenter.onFileLongClicked(file)
    }

    override fun selectOrUnSelectItem(file: FileDataModel) {
        view.selectOrUnSelect(file)
    }

    override fun selectAll() {
        view.selectAll()
    }

    override fun getAllItems(): List<FileDataModel> {
        return view.getAllItems()
    }

    override fun getSelectedItems(): List<FileDataModel> {
        return view.getSelectedItems()
    }

    override fun getSelectedItemCount(): Int {
        return getSelectedItems().count()
    }

    override fun stopActionMode() {
        view.stopActionMode()
    }

//    override fun locate(file: FileDataModel) {
//    }
}