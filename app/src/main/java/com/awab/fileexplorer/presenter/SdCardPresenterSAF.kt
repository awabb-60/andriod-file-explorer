package com.awab.fileexplorer.presenter

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.model.utils.PICKER_REQUEST_CODE
import com.awab.fileexplorer.model.utils.SD_CARD_TREE_URI_SP
import com.awab.fileexplorer.model.utils.TREE_URI_
import com.awab.fileexplorer.model.utils.navigateToTreeFile
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
    override val storageName: String = File(storagePath).name

    override var targetesUnAuthorizedSDCardName: String = storageName

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
            val oldName = File(path).name
            val sdCardDocumentFile = getTreeUriFile() ?: return
            val file = navigateToTreeFile(sdCardDocumentFile, path)
            if (file != null) {
                val success = file.renameTo(newName)
//                    some error occur... the file was node found or couldn't delete this file
                if (!success)
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
        if (!isAuthorized()) {
            requestPermission()
            return
        }
        try {
//        cutting the parent directory path from sd card path
            val newFolderName = path.split('/').last().trim()

            val sdCardDocumentFile = getTreeUriFile() ?: return

//        navigating to the parent
            val parentFolder = navigateToTreeFile(sdCardDocumentFile, File(path).parent)

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

    override fun delete(showMessages: Boolean) {
        try {
            val selectedItems = supPresenter.getSelectedItems()
            val sdCardDocumentFile = getTreeUriFile() ?: return

            // the parent folder that contains the files that will get deleted
            val parentFolder = navigateToTreeFile(sdCardDocumentFile, File(selectedItems[0].path).parent)
            if (selectedItems.isEmpty() || parentFolder == null)
                return

            view.loadingDialog.show()
            // deleting the files
            DeleteFromSdCardAsyncTask(parentFolder, object : SimpleSuccessAndFailureCallback<Boolean> {
                override fun onSuccess(data: Boolean) {
                    view.loadingDialog.dismiss()
                    if (!data && showMessages)
                        view.showToast("some error occur while deleting the files")
                    else if (data && showMessages)
                        view.showToast("items successfully deleted")

                    supPresenter.loadFiles()
                }

                // this called when file some files are not deleted
                // so the loading ui will stile going
                override fun onFailure(message: String) {
                    if (showMessages)
                        view.showToast(message)
                }
            }).execute(selectedItems)

            view.stopActionMode()
        } catch (e: Exception) {
            if (showMessages)
                if(showMessages)
                Toast.makeText(view.context(), "error deleting files", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTreeUriFile(): DocumentFile? {
        return DocumentFile.fromTreeUri(view.context(), getTreeUri(view.context(), storageName))
    }

    @RequiresApi(19)
    private fun openPicker() {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        view.openAuthorizationPicker(intent, PICKER_REQUEST_CODE)
    }


    private fun sdCardAuthorized(): Boolean {
        val sp = view.context()
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE)

        return if (sp.contains(TREE_URI_ + storageName)) {
            // updating the permission
            val uri = getTreeUri(view.context(), storageName)
            view.context().grantUriPermission(
                view.context().packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            true
        } else
            false
    }
}