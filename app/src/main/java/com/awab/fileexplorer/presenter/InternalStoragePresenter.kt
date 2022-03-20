package com.awab.fileexplorer.presenter

import com.awab.fileexplorer.model.MainStorageModel
import com.awab.fileexplorer.model.utils.createFolderIO
import com.awab.fileexplorer.model.utils.renameFileIO
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.presenter.contract.SupPresenter
import com.awab.fileexplorer.utils.INTERNAL_STORAGE_REQUIRED_PERMISSIONS
import com.awab.fileexplorer.utils.allPermissionsGranted
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.types.StorageType
import com.awab.fileexplorer.utils.storageAccess
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File

class InternalStoragePresenter(
    private val storageView: StorageView,
    private val _storagePath: String
) : StoragePresenterContract {

    override val storagePath: String
        get() = _storagePath

    override val storageName: String
        get() = File(storagePath).name

    override var targetedUnAuthorizedSDCardName: String = ""

    override lateinit var supPresenter: SupPresenter

    override var actionModeOn: Boolean = false

    override val view: StorageView
        get() = storageView

    override val model = MainStorageModel(view.context())

    override fun isAuthorized(): Boolean {
        return allPermissionsGranted(view.context(), INTERNAL_STORAGE_REQUIRED_PERMISSIONS)
    }

    override fun requestPermission() {
        storageAccess(view.context())
    }

    override fun rename(path: String, newName: String) {
        if (renameFileIO(path, newName)) {
            view.showToast("file renamed successfully")

            view.stopActionMode()
            supPresenter.loadFiles()
        } else
            view.showToast("cant rename this file")
    }

    override fun createFolder(path: String) {
        if (createFolderIO(File(path.trim())))
            supPresenter.loadFiles()
        else
            view.showToast("folder was not crated")
    }

    override fun delete(showMessages:Boolean) {
        view.loadingDialog.show()
        model.deleteFromInternalStorage(
            supPresenter.getSelectedItems(),
            StorageType.INTERNAL,
            object : SimpleSuccessAndFailureCallback<Boolean> {
                override fun onSuccess(data: Boolean) {
                    view.loadingDialog.dismiss()
                    if (data && showMessages)
                        view.showToast("files deleted successfully")
                    supPresenter.loadFiles()
                }

                // this called when file some files are not deleted
                // so the loading ui will stile going
                override fun onFailure(message: String) {
                    if (showMessages)
                        view.showToast(message)
                }
            })
        view.stopActionMode()
    }
}