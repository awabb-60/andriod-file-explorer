package com.awab.fileexplorer.view.contract

import android.content.Context
import com.awab.fileexplorer.model.data_models.FileModel

interface IFileFragmentView {

    fun context(): Context

    fun updateList(list: List<FileModel>)

    fun selectOrUnSelectItem(file: FileModel)

    fun stopActionMode()

    fun selectAll()

    fun getSelectedItems():List<FileModel>
}
