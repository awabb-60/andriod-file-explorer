package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.view.contract.HomeView

interface HomePresenterContract {

    /**
     * this presenter view
     */
    val view: HomeView

    /**
     * open the storage in a new activity
     */
    fun openStorage(it: StorageDataModel)

    /**
     * callback when a media item is clicked
     */
    fun mediaItemClicked(id: Int)

    /**
     * make storage models for the available storages in the device
     */
    fun makeStoragesModels()
}