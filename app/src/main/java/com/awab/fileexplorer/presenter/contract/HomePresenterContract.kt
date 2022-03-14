package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.model.contrancts.StorageModel
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.view.contract.HomeView

interface HomePresenterContract {

    /**
     * this presenter view
     */
    val view: HomeView

    val model: StorageModel

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
    fun loadStorages()

    /**
     * returns the pined files from the database
     */
    fun loadPinedFiles()

    /**
     * returns the recent files from the database
     */
    fun loadRecentFiles()

    /**
     * set the height of the quick access card
     * @param cardHeight the new height
     */
    fun setQuickAccessFilesCardHeight(cardHeight: Int)

}