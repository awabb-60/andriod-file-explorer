package com.awab.fileexplorer.view.contract

import android.content.Context
import android.content.Intent
import com.awab.fileexplorer.adapters.MediaAdapter
import com.awab.fileexplorer.model.data_models.MediaItemModel

interface MediaView {

    val mediaAdapter:MediaAdapter

    fun context():Context

    fun openFile(intent: Intent)

    fun startActionMode()

    fun updateActionMode()

    fun stopActionMode()

    fun pressBack()

    fun showDetails(name: String, path: String, size: String, dateStr: String)

    fun showDetails(contains: String, totalSize:String)

    fun showToast(message: String)
}