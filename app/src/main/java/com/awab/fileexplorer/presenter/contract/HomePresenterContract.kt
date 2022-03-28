package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.model.contrancts.StorageModel
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.view.contract.HomeView

interface HomePresenterContract {

    /**
     * this presenter view
     */
    val view: HomeView

    val model: StorageModel

    /**
     * this tell wither the quick access window in edit mode or not
     */
    var quickAccessInEditMode: Boolean

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
     * update the height of the quick access card adjusting the new list of files
     * @param list the new list
     */
    fun updateQuickAccessCard(list: List<QuickAccessFileDataModel>)

    /**
     * set the new height of the quick access card
     */
    fun updateQuickAccessCardHeight(fileDim: Int, listSize: Int)

    /**
     * get called after an item is clicked in the quick access rv
     * @param file the targeted file
     */
    fun quickAccessItemClicked(file: QuickAccessFileDataModel)

    /**
     * get called after an item is long clicked in the quick access rv
     * @param file the targeted file
     */
    fun quickAccessItemLongClicked(file: QuickAccessFileDataModel)

    /**
     * delete the file form the recent or pined files
     * @param file the file that will get deleted
     */
    fun deleteQuickAccessFile(file: QuickAccessFileDataModel)

    fun quickAccessEditModeStopped()

    /**
     * navigate to the containing folder in the file system
     */
    fun locateFile(path: String)

    /**
     * the the saved view settings for the app
     */
    fun setViewSettings()
}