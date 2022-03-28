package com.awab.fileexplorer.view.contract

import android.content.Context
import android.content.Intent
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel

interface HomeView {

    /**
     * open an new activity with the given intent
     */
    fun openActivity(intent: Intent)

    /**
     * return the context of this view
     */
    fun context():Context

    /**
     * put and update the storages items
     */
    fun updateStoragesList(storages: Array<StorageDataModel>)

    /**
     * to update the list of file in the quick access window
     */
    fun updateQuickAccessFilesList(list: List<QuickAccessFileDataModel>)

    /**
     * to adjust the quick access window to show that the list is empty
     */
    fun quickAccessIsEmpty()

    /**
     * update the quick access window height
     */
    fun updateQuickAccessCardHeight(cardHeight: Int)

    /**
     * show the edit button in the quick access window
     */
    fun showEditQuickAccess()

    /**
     * show the details of a file in a dialog
     */
    fun showDetailsDialog(name: String, size: String, lastModified: String, path: String)
}