package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.view.contract.IFileFragmentView

interface FilesListPresenterContract {

    val actionModeOn:Boolean

    val view: IFileFragmentView

    val filesList:List<FileModel>

    fun loadFiles()

    fun removeBreadcrumb()

    fun onFileClick(file: FileModel)

    fun onFileLongClick(file: FileModel)

    fun selectOrUnClickedItem(file: FileModel)

    fun selectAll()

    fun getSelectedItems():List<FileModel>

    fun stopActionMode()

}