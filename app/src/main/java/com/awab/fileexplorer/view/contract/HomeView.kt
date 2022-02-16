package com.awab.fileexplorer.view.contract

import android.content.Context
import android.content.Intent
import com.awab.fileexplorer.model.data_models.StorageModel

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
     * asks the user for permissions to use the app
     */
    fun checkForPermissions()

    /**
     * put and update the storages items
     */
    fun updateStoragesList(storages: Array<StorageModel>)
}