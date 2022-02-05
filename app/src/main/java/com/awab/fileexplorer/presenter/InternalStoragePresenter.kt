package com.awab.fileexplorer.presenter

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.awab.fileexplorer.model.types.FileType
import com.awab.fileexplorer.presenter.contract.FilesListPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.presenter.contract.SearchPresenterContract
import com.awab.fileexplorer.presenter.contract.SupPresenter
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File

class InternalStoragePresenter(
    private val storageView: StorageView,
    private val storageName: String,
    private val storagePath: String
) : StoragePresenterContract {

    val copyLocation = ""

    override lateinit var supPresenter: SupPresenter

    override var actionModeOn: Boolean = false

    override val view: StorageView
        get() = storageView

    override fun saveTreeUri(treeUri: Uri) {
        Log.d("TAG", "saveTreeUri: internal saved")
    }

    override fun isAuthorized(): Boolean {
        return allPermissionsGranted(view.context(), INTERNAL_STORAGE_REQUIRED_PERMISSIONS)
    }

    override fun requestPermission() {
        storageAccess(view.context())
    }

    override fun rename(path: String, newName: String) {
        if (renameFileIO(path, newName)) {
            Toast.makeText(view.context(), "file renamed successfully", Toast.LENGTH_SHORT).show()

            view.stopActionMode()
            supPresenter.loadFiles()
        } else
            Toast.makeText(view.context(), "cant rename this file", Toast.LENGTH_SHORT).show()
    }

    override fun createFolder(path: String) {
        if (createFolderIO(File(path.trim())))
            supPresenter.loadFiles()
        else
            Toast.makeText(view.context(), "folder was not crated", Toast.LENGTH_SHORT).show()
    }

    override fun delete() {
        try {
            val selectedItems = supPresenter.getSelectedItems()
            selectedItems.forEach {
                if (it.type == FileType.DIRECTORY)
                    deleteFolderIO(it.path)
                else
                    deleteFileIO(it.path)
            }

            view.stopActionMode()
            supPresenter.loadFiles()
        } catch (e: Exception) {
            Toast.makeText(view.context(), "error deleting files", Toast.LENGTH_SHORT).show()
        }
    }

    //    not really used
    override fun isValidTreeUri(treeUri: Uri): Boolean {
        return true
    }
}