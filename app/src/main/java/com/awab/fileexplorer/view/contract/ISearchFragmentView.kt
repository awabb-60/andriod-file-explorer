package com.awab.fileexplorer.view.contract

import android.content.Context
import android.view.ContextThemeWrapper
import com.awab.fileexplorer.model.data_models.FileModel

interface ISearchFragmentView {

    fun context(): Context

    fun isReady()

    fun showSearchList(list:List<FileModel>, searchText:String)

    fun searchResultEmpty()

    fun searchTextEmpty()

    fun removeInputMethod()
}