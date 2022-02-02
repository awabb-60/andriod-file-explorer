package com.awab.fileexplorer.presenter

import android.os.AsyncTask
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.utils.getInnerFiles
import com.awab.fileexplorer.model.utils.getSearchResults
import com.awab.fileexplorer.model.utils.makeFileModels
import com.awab.fileexplorer.view.contract.ISearchFragmentView
import java.io.File

class SearchFragmentPresenter(private val view: ISearchFragmentView, folderName: String) {

    private var searchList = listOf<FileModel>()

    init {
        SearchListAsync(this, folderName).execute()
    }

    fun searchTextChanged(text: String) {
        if (text.isBlank())
            view.searchTextEmpty()
        else {
            val searchResults = getSearchResults(searchList, text)
            updateViewList(searchResults, text)
        }
    }

    private fun updateViewList(list: List<FileModel>, text:String) {
        if (list.isEmpty())
            view.searchResultEmpty()
        else
            view.showSearchList(list, text)
    }

    private fun isReady(list: List<FileModel>) {
        searchList = list
        view.isReady()
    }

    class SearchListAsync(private val controller: SearchFragmentPresenter, private val folderName: String) :
        AsyncTask<Unit, Unit, Unit>() {

        override fun doInBackground(vararg params: Unit?) {
            val folder = File(folderName)
            val allFiles = getInnerFiles(folder, false)
            val initialList = makeFileModels(allFiles)
            controller.isReady(initialList)
        }
    }
}