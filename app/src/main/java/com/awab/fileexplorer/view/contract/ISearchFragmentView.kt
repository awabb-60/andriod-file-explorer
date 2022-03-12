package com.awab.fileexplorer.view.contract

import android.content.Context
import com.awab.fileexplorer.utils.data.data_models.FileDataModel

interface ISearchFragmentView {

    fun context(): Context

    fun isReady()

    fun showSearchList(list:List<FileDataModel>, searchText:String)

    fun searchResultEmpty()

    fun searchTextEmpty()

    fun removeInputMethod()

    fun selectOrUnSelect(file: FileDataModel)

    fun selectAll()

    fun getSelectedItems(): List<FileDataModel>

    fun stopActionMode()

    fun finishFragmnet()
}