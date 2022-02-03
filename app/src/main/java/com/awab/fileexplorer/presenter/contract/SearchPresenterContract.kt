package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.model.data_models.FileModel

interface SearchPresenterContract {

    var searchList:List<FileModel>

    val mainStoragePresenter:StoragePresenterContract

    fun onTextChanged(text:String)

    fun loadFiles()

    fun isReady(list:List<FileModel>)

    fun onItemClicked(file: FileModel)
}