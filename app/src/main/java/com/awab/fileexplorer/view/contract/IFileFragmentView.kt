package com.awab.fileexplorer.view.contract

import android.content.Context
import com.awab.fileexplorer.utils.data.data_models.FileDataModel

interface IFileFragmentView {

    fun context(): Context

    fun updateList(list: List<FileDataModel>)

    fun selectOrUnSelectItem(file: FileDataModel)

    fun stopActionMode()

    fun selectAll()

    fun getSelectedItems():List<FileDataModel>
}
