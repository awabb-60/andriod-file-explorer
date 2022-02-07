package com.awab.fileexplorer.view.contract

import android.content.Context
import com.awab.fileexplorer.model.data_models.FileModel

interface ISearchFragmentView {

    fun context(): Context

    fun isReady()

    fun showSearchList(list:List<FileModel>, searchText:String)

    fun searchResultEmpty()

    fun searchTextEmpty()

    fun removeInputMethod()

    fun selectOrUnSelect(file: FileModel)

    fun selectAll()

    fun getSelectedItems(): List<FileModel>

    fun stopActionMode()

    fun finishFragmnet()
}