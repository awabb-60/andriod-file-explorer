package com.awab.fileexplorer.presenter

import android.content.Intent
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.model.MainStorageModel
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.presenter.contract.SupPresenter
import com.awab.fileexplorer.utils.PICKER_REQUEST_CODE
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.navigateToTreeFile
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

    override var targetedUnAuthorizedSDCardName: String = storageName

    override lateinit var supPresenter: SupPresenter

    override var actionModeOn: Boolean = false

    override val view: StorageView
        get() = storageView

    override val model = MainStorageModel(view.context())

    override fun isAuthorized(): Boolean {
        return sdCardAuthorized()
    }

    override fun requestPermission() {
        view.showToast("select the sd card")
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

            val sdCardDocumentFile = getTreeUriFile() ?: error("cant make parent tree document file")

//        navigating to the parent
            val folder = File(path).parent ?: error("cant make this files")
            val parentFolder = navigateToTreeFile(sdCardDocumentFile, folder)

//        creating the new directory and refreshing
            if (parentFolder != null) {
                val success = parentFolder.createDirectory(newFolderName)
                if (success != null)
                    supPresenter.loadFiles()
                else
                    view.showToast("unable to create this file")
            }
        } catch (e: Exception) {
            view.showToast("unable to create this file")
        }
    }

    override fun delete(showMessages: Boolean) {
        val selectedItems = supPresenter.getSelectedItems()
        val sdCardDocumentFile = getTreeUriFile() ?: return

        // the parent folder that contains the files that will get deleted
        val folder = File(selectedItems[0].path).parent ?: return
        val parentFolder = navigateToTreeFile(sdCardDocumentFile, folder)
        if (selectedItems.isEmpty() || parentFolder == null)
            return

        view.loadingDialog.show()
        // deleting the files
        model.deleteFromSdCard(selectedItems, parentFolder, object : SimpleSuccessAndFailureCallback<Boolean> {
            override fun onSuccess(data: Boolean) {
                view.loadingDialog.dismiss()
                if (!data && showMessages)
                    view.showToast("some error occur while deleting the files")
                else if (data && showMessages)
                    view.showToast("items successfully deleted")

                supPresenter.loadFiles()
            }

            override fun onFailure(message: String) {
                if (showMessages)
                    view.showToast(message)
            }
        })
        view.stopActionMode()
    }

    private fun getTreeUriFile(): DocumentFile? {
        val uri = model.getTreeUri(storageName) ?: return null
        return DocumentFile.fromTreeUri(view.context(), uri)
    }

    private fun openPicker() {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        view.openAuthorizationPicker(intent, PICKER_REQUEST_CODE)
    }

    private fun sdCardAuthorized(): Boolean {
        return if (model.getTreeUri(storageName) != null) {
            // updating the permission
//            val uri =
//            view.context().grantUriPermission(
//                view.context().packageName,
//                uri,
//                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//            )
            true
        } else
            false
    }
}