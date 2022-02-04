package com.awab.fileexplorer.presenter

import android.content.ContentResolver
import android.os.AsyncTask
import android.provider.MediaStore
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.utils.getSearchResults
import com.awab.fileexplorer.model.utils.makeFileModels
import com.awab.fileexplorer.presenter.contract.SearchPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.view.contract.ISearchFragmentView
import java.io.File

class SearchFragmentPresenter(
    private val view: ISearchFragmentView,
    private val folderPath: String,
    private val mStoragePresenter: StoragePresenterContract
) : SearchPresenterContract {

    override val mainStoragePresenter: StoragePresenterContract
        get() = mStoragePresenter


    override var searchList: List<FileModel> = listOf()

    override fun loadFiles() {
        if (searchList.isNullOrEmpty())
            SearchListAsync(this, folderPath, view.context().contentResolver).execute()
        else
            isReady(searchList)
    }

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

    override fun onItemClicked(file: FileModel) {
        view.removeInputMethod()
        mainStoragePresenter.onFileClickedFromSearch(file)
    }

    class SearchListAsync(
        private val presenter: SearchFragmentPresenter,
        private val folderPath: String,
        private val contentResolver: ContentResolver
    ) :
        AsyncTask<Unit, Unit, List<FileModel>>() {

        override fun doInBackground(vararg params: Unit?): List<FileModel> {
//            val folder = File(folderPath)
//            val allFiles = getInnerFiles(folder, false)

            val query = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                null, "_data Like ?",
                arrayOf("%$folderPath%"), null
            )

            val allFiles = mutableListOf<File>()
            query?.let { cursor ->
                cursor.use {
                    val pathId = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    while (it.moveToNext()) {
                        val path = it.getString(pathId)
                        allFiles.add(File(path))
                    }
                }
            }
            return makeFileModels(allFiles)
        }

        override fun onPostExecute(result: List<FileModel>) {
            presenter.isReady(result)
            super.onPostExecute(result)
        }
    }
}