package com.awab.fileexplorer.presenter.contract

import android.content.Intent
import com.awab.fileexplorer.model.data_models.MediaItemModel
import com.awab.fileexplorer.view.contract.MediaView

interface MediaPresenterContract {

    val view: MediaView

    var actionModeOn: Boolean

    fun loadMedia(intent: Intent)

    fun mediaItemClicked(item: MediaItemModel)

    fun mediaItemLongClicked(item: MediaItemModel)

    fun processClick(item: MediaItemModel)

    fun getOpenFileIntent(file: MediaItemModel): Intent

    fun getActionModeTitle(): String

    fun stopActionMode()

    fun oneItemSelected(): Boolean

    fun showDetails()

    fun selectAll()

    fun openWith()
}