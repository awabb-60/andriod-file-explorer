package com.awab.fileexplorer.model.contrancts

import android.net.Uri
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.PinedFileDataModel
import com.awab.fileexplorer.utils.data.data_models.RecentFileDataModel

/**
 * the model contract
 */
interface StorageModel {

    /**
     * save the uri of the sd card to the shared preferences for later us
     */
    fun saveTreeUri(treeUri: Uri, sdCardName: String)

    /**
     * it will load the saved tree uri of the sd card from the shared preferences
     * @return the tree uri of the sd card
     */
    fun getTreeUri(storageName: String): Uri


    /**
     * it will return the saved sort argument
     * @return the a string that represent the sort argument
     */
    fun viewSortBySettings(): String?

    /**
     * it will return the saved sort order
     * @return the a string that represent the sort order
     */
    fun viewSortOrderSettings(): String?

    /**
     * it will return the saved setting for showing the hidden file
     * @return true to show the hidden file, false otherwise
     */
    fun viewHiddenFilesSettings(): Boolean

    /**
     * save the view settings to the shared preferences.
     * @param sortBy the sort argument.
     * @param order the sort order.
     * @param showHiddenFiles the hidden files visibility.
     */
    fun saveViewingSettings(sortBy: String, order: String, showHiddenFiles: Boolean)

    /**
     * saves the file to the recent files database
     */
    fun saveToRecentFiles(list: List<FileDataModel>)

    /**
     * saves the file to the pined files database
     */
    fun saveToPinedFiles(list: List<FileDataModel>)

    /**
     * return the saved pined files  from the database
     */
    fun getPinedFiles(callback: SimpleSuccessAndFailureCallback<List<PinedFileDataModel>>)

    /**
     * return the saved pined files  from the database
     */
    fun getRecentFiles(callback: SimpleSuccessAndFailureCallback<List<RecentFileDataModel>>)
}