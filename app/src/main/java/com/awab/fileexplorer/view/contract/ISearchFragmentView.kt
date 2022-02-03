package com.awab.fileexplorer.view.contract

import com.awab.fileexplorer.model.data_models.FileModel

interface ISearchFragmentView {

    fun isReady()

    fun showSearchList(list:List<FileModel>, searchText:String)

    fun searchResultEmpty()

    fun searchTextEmpty()

    fun removeInputMethod()
}