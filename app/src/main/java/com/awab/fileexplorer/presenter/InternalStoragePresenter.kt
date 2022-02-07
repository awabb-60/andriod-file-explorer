package com.awab.fileexplorer.presenter

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.presenter.contract.SupPresenter
import com.awab.fileexplorer.presenter.threads.DeleteFromInternalStorageAsyncTask
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File

class InternalStoragePresenter(
    private val storageView: StorageView,
    private val _storagePath: String
) : StoragePresenterContract {


    override val storagePath: String
        get() = _storagePath

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
        view.loadingDialog.show()
        DeleteFromInternalStorageAsyncTask(object : SimpleSuccessAndFailureCallback<Boolean> {
            override fun onSuccess(data: Boolean) {
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
        })
        view.stopActionMode()
    }

    //    not really used
    override fun isValidTreeUri(treeUri: Uri): Boolean {
        return true
    }
}