package com.awab.fileexplorer.model.contrancts

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.FilesDetailsDataModel
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.types.QuickAccessFileType
import com.awab.fileexplorer.utils.data.types.StorageType

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
    fun getTreeUri(storageName: String): Uri?


    /**
     * it will return the saved sort argument
     * @return the a string that represent the sort argument
     */
    fun viewSortBySettings(): String

    /**
     * it will return the saved sort order
     * @return the a string that represent the sort order
     */
    fun viewSortOrderSettings(): String

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
     * saves the file to the pined files database
     */
    fun saveToQuickAccessFiles(list: List<QuickAccessFileDataModel>)

    /**
     * return the saved pined files  from the database
     */
    fun getQuickAccessFiles(
        targetedType: QuickAccessFileType,
        callback: SimpleSuccessAndFailureCallback<List<QuickAccessFileDataModel>>
    )

    fun deleteQuickAccessFile(file: QuickAccessFileDataModel, callback: SimpleSuccessAndFailureCallback<Boolean>?)

    /**
     * run a query with the given projection to load all the files in it
     */
    fun queryFiles(
        contentUri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        callback: SimpleSuccessAndFailureCallback<List<FileDataModel>>
    )

    /**
     * to cancel the running query of the files
     */
    fun cancelQueryFiles()


    /**
     * return a FilesDetailsDataModel from the given list of list
     * @param list the list of files to get it details
     * @return FilesDetailsDataModel object tha has the details of the list of list
     */
    fun getFilesDetails(
        list: List<FileDataModel>,
        callback: SimpleSuccessAndFailureCallback<FilesDetailsDataModel>
    )

    /**
     * get the details of the media files
     */
    fun getSelectedMediaDetails(
        list: List<FileDataModel>,
        callback: SimpleSuccessAndFailureCallback<FilesDetailsDataModel>
    )

    /**
     * cancel the the job of getting the details
     */
    fun cancelGetDetails()

    /**
     * delete all the given files from the internal storage using basics java utilities
     * @param list the list of files to delete
     */
    fun deleteFromInternalStorage(
        list: List<FileDataModel>,
        storageType: StorageType,
        callback: SimpleSuccessAndFailureCallback<Boolean>
    )

    /**
     * delete all the given files in the list from the sdCard storage
     * @param list the list of files to delete
     * @param parentFolder the parent document file that contains the targeted files for deletion
     * if the parentFolder does not contain the targeted files, failure callback will get invoked
     */
    fun deleteFromSdCard(
        list: List<FileDataModel>,
        parentFolder: DocumentFile,
        callback: SimpleSuccessAndFailureCallback<Boolean>
    )
}