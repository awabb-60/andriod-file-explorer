package com.awab.fileexplorer.presenter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.model.utils.PICKER_REQUEST_CODE
import com.awab.fileexplorer.model.utils.SD_CARD_TREE_URI_SP
import com.awab.fileexplorer.model.utils.TREE_URI_
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.presenter.contract.SupPresenter
import com.awab.fileexplorer.presenter.threads.DeleteFromSdCardAsyncTask
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File

/**
 * this presenter is for device with api level 19 - 30
 * it uses the SAF framework to write to the sd card
 */
class SdCardPresenterSAF(
    private val storageView: StorageView,
    private val _storagePath: String
) : StoragePresenterContract {

    override val storagePath: String
        get() = _storagePath

    // the name of the sd card
    private val storageName: String = File(storagePath).name

    override lateinit var supPresenter: SupPresenter

    override var actionModeOn: Boolean = false

    override val view: StorageView
        get() = storageView

    override fun isAuthorized(): Boolean {
        return sdCardAuthorized()
    }

    override fun requestPermission() {
        view.showToast("select the sd card")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            openPicker()
    }

    override fun rename(path: String, newName: String) {
        try {
            val oldName = path.split(File.pathSeparatorChar).last()
            val sdCardDocumentFile = getTreeUriFile()?:return
            val parentFolder = navigateToParentTreeFile(sdCardDocumentFile, path)
            if (parentFolder != null) {
                val success = parentFolder.findFile(oldName)?.renameTo(newName)
//                    some error occur... the file was node found or couldn't delete this file
                if (success == null || !success)
                    view.showToast("error renaming $oldName")
            } else { //  error navigating to the file
                view.showToast("error renaming $oldName")
            }

            view.stopActionMode()
            supPresenter.loadFiles()
        } catch (e: Exception) {
            view.showToast("IO error occurred")
        }
    }

    override fun createFolder(path: String) {
        if (!isAuthorized()){
            requestPermission()
            return
        }
        try {
//        cutting the parent directory path from sd card path
            val newFolderName = path.split('/').last().trim()

            val sdCardDocumentFile = getTreeUriFile()?:return

//        navigating to the parent
            val parentFolder = navigateToParentTreeFile(sdCardDocumentFile, path)

//        creating the new directory and refreshing
            if (parentFolder != null) {
                val success = parentFolder.createDirectory(newFolderName)
                if (success != null)
                    supPresenter.loadFiles()
                else
                    Toast.makeText(view.context(), "unable to create this file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(view.context(), "unable to create this file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun delete() {
        try {
            val selectedItems = supPresenter.getSelectedItems()
            val sdCardDocumentFile = getTreeUriFile()?:return

            // the parent folder that contains the files that will get deleted
            val parentFolder = navigateToParentTreeFile(sdCardDocumentFile, selectedItems[0].path)
            if (selectedItems.isEmpty() || parentFolder == null)
                return

            view.loadingDialog.show()
            // deleting the files
            DeleteFromSdCardAsyncTask(parentFolder, object : SimpleSuccessAndFailureCallback<Boolean> {
                override fun onSuccess(data: Boolean) {
                    view.loadingDialog.dismiss()
                    if (!data)
                        view.showToast("some error occur while deleting the files")
                    else
                        view.showToast("items deleted successfully")

                    supPresenter.loadFiles()
                }
                // this called when file some files are not deleted
                // so the loading ui will stile going
                override fun onFailure(message: String) {
                    view.showToast(message)
                }
            }).execute(selectedItems)

            view.stopActionMode()
        } catch (e: Exception) {
            Toast.makeText(view.context(), "error deleting files", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTreeUriFile(): DocumentFile? {
        return DocumentFile.fromTreeUri(view.context(), getTreeUri(view.context(), storageName))
    }

    /**
     * this return the folder inside the treeDocumentFile that contains the file with the given file path
     */
    private fun navigateToParentTreeFile(treeDocumentFile: DocumentFile, filePath: String): DocumentFile? {
//        removing the name from the path
        var innerPath = filePath.dropLastWhile { it != File.separatorChar }

//        removing the tree storage name  (sd card)
        val storageName = treeDocumentFile.name?:return null

        // the file path without the sd card storage path at the start
        innerPath = innerPath.removeRange(0, filePath.indexOf(storageName) + storageName.length + 1)

        // the sd card storage tree document file
        var file:DocumentFile? = treeDocumentFile

//        navigating to it parent
        for (fileName in innerPath.split(File.separatorChar).filter { it != "" }) {
            file = file?.findFile(fileName)
            if (file == null)
                break
        }
        return file
    }

    @RequiresApi(19)
    private fun openPicker() {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        view.openAuthorizationPicker(intent, PICKER_REQUEST_CODE)
    }

    override fun isValidTreeUri(treeUri: Uri): Boolean {
        DocumentFile.fromTreeUri(view.context(), treeUri)?.name?.let { sdCardName ->
            return sdCardName == storageName
        }
        return false
    }

    private fun sdCardAuthorized(): Boolean {
        val sp = view.context()
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE)
        return sp.contains(TREE_URI_ + storageName)
    }

    override fun saveTreeUri(treeUri: Uri) {
        val spE = view.context()
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE).edit()
        spE.putString(TREE_URI_ + storageName, treeUri.toString())
        spE.apply()
    }
}