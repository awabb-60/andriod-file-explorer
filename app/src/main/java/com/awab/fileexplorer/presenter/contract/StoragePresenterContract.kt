package com.awab.fileexplorer.presenter.contract

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.awab.fileexplorer.model.RecentFiles
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.data_models.SelectedItemsDetailsModel
import com.awab.fileexplorer.model.types.FileType
import com.awab.fileexplorer.model.types.MimeType
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.presenter.threads.SelectedFilesDetailsAsyncTask
import com.awab.fileexplorer.presenter.threads.SelectedMediaDetailsAsyncTask
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File
import java.text.SimpleDateFormat

interface StoragePresenterContract {

    // the name of your sd card
    val sdCardName: String
        get() = ""

    // the path of your sd card
    val sdCardPath: String
        get() = ""

    // the path of your sd card
    val internalStoragePath: String
        get() = ""

    var supPresenter: SupPresenter

    var actionModeOn: Boolean

    val view: StorageView

    /**
     * this method bind thr supPresenter to the main presenter
     */
    fun bindSupPresenter(supPresenter: SupPresenter) {
        this.supPresenter = supPresenter
        // only updating when the state is different
        if (supPresenter.mainMenuState != view.showMenu)
            updateMaineMenu(supPresenter.mainMenuState)
    }

    /**
     * show or hide the main menu based on visibility
     * @param visible the menu visibility state
     */
    fun updateMaineMenu(visible: Boolean) {
        view.showMenu = visible
        view.updateMenu()
    }

    /**
     * to check is the storage is authorized
     * mainly for sd card
     */
    fun isAuthorized(): Boolean

    fun saveTreeUri(treeUri: Uri)

    fun requestPermission()

    fun isValidTreeUri(treeUri: Uri): Boolean

    fun createFolder(path: String)

    fun confirmDelete() {
        view.confirmDelete()
    }

    fun delete()

    fun showMIRename(): Boolean {
//        true to show rename
        val count = supPresenter.getSelectedItemCount()
        return count == 1
    }

    fun removeBreadcrumb() {
        view.removeBreadcrumb()
    }

    fun getActionModeTitle(): String {
        val count = supPresenter.getSelectedItemCount()

        return if (count <= 1)
            "$count item Selected"
        else
            "$count items Selected"
    }

    fun selectAll() {
        supPresenter.selectAll()
        view.updateActionMode()
    }

    fun shouldStopActionMode(): Boolean {
        val count = supPresenter.getSelectedItemCount()
        return count <= 0
    }

    fun stopActionMode() {
        actionModeOn = false
        view.stopActionMode()
        supPresenter.stopActionMode()
    }

    fun showDetails() {
        val items = supPresenter.getSelectedItems()

        if (items.size == 1) {
            val item = items[0]
            val date = SimpleDateFormat(DATE_FORMAT_PATTERN).format(item.date)
            if (item.type == FileType.FILE) {
                view.showDetails(item.name, date, item.size, item.path)
            } else {
                val sizeInBytes = getFolderSizeBytes(File(item.path))
                val size = getSize(sizeInBytes)
                view.showDetails(item.name, date, size, item.path)
            }
        } else {
            // loading large files on a background thread
            view.loadingDialog.show()
            SelectedFilesDetailsAsyncTask(object:SimpleSuccessAndFailureCallback<SelectedItemsDetailsModel>{
                override fun onSuccess(data: SelectedItemsDetailsModel) {
                    view.showDetails(data.contains, data.totalSize)
                    view.loadingDialog.dismiss()
                }
                override fun onFailure(message: String) {
                    view.loadingDialog.dismiss()
                }
            }).execute(items)
        }

    }

    fun confirmRename() {
        val item = supPresenter.getSelectedItems().get(0)
        view.showRenameDialog(item.path, item.name)
    }

    fun rename(path: String, newName: String)

    fun onFileClicked(file: FileModel) {
//        selecting unselecting the item
        if (actionModeOn) {
            supPresenter.selectOrUnSelectItem(file)
            view.updateActionMode()
            return
        }

//        opining the item
        if (file.type == FileType.FILE) {
//            file cant be opened
            if (file.mimeType == MimeType.UNKNOWN) {
                Toast.makeText(view.context(), "unsupported file format", Toast.LENGTH_SHORT).show()
                return
            }
//            opening the file
            view.openFile(getOpenFileIntent(file))
        } else // navigating to folder
            view.navigateToFolder(file.name, file.path)
    }

    fun onFileClickedFromSearch(file: FileModel) {
        if (file.type == FileType.DIRECTORY) {
            view.showMenu = false
            view.updateMenu()
        }

        onFileClicked(file)
    }

    fun onFileLongClicked(file: FileModel) {
        // long click a selected item do nothing
        if (file.selected)
            return
//        starting the action mode
        if (actionModeOn) {
            // selecting unselecting the item
            supPresenter.selectOrUnSelectItem(file)
            view.updateActionMode()
            return
        }
        actionModeOn = true
        supPresenter.selectOrUnSelectItem(file)
        view.startActionMode()
    }

    fun getOpenFileIntent(file: FileModel): Intent {
        RecentFiles.recentFilesList.add(file.path)
        if (file.mimeType == MimeType.APPLICATION) {
            return Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(file.uri, file.mimeType.mimeString)
            }
        }
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, file.mimeType.mimeString)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun startCopyScreen() {
        val selectedItem = supPresenter.getSelectedItems()
        if (selectedItem.isEmpty())
            return
        view.startCopyScreen()
    }

    fun startMoveScreen() {
        val selectedItem = supPresenter.getSelectedItems()
        if (selectedItem.isEmpty())
            return
        view.startMoveScreen()
    }

    // to copy or move to the sd card or the internal storage, you have to have a folder
    // named (Paste) in location you try to copy to.
    fun copy(to: String) {
        val selectedItem = supPresenter.getSelectedItems()
        if (selectedItem.isEmpty())
            return

        val listPath = selectedItem.map { it.path }
        view.openCopyProgress("Copying")

        val intent = when (to) {
            "I" -> {
                Intent(COPY_BROADCAST_ACTION).apply {
                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    //  the copy location
                    putExtra(PASTE_LOCATION, internalStoragePath + File.separator + "Paste")
                }
            }
            "S" -> {
                Intent(COPY_BROADCAST_ACTION).apply {
                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    //  the copy location
                    putExtra(PASTE_LOCATION, sdCardPath + File.separator + "Paste")
                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
                }
            }

            else -> Intent("Noop")
        }

        intent.putExtra(COPY_TYPE_EXTRA, COPY)
//        starting the copy service
        view.context().sendBroadcast(intent)
    }

    fun move(to: String) {
        val selectedItem = supPresenter.getSelectedItems()
        if (selectedItem.isEmpty())
            return

        val listPath = selectedItem.map { it.path }
        view.openCopyProgress("Moving")

        val intent = when (to) {
            "I" -> {
                Intent(COPY_BROADCAST_ACTION).apply {
                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    //  the copy location
                    putExtra(PASTE_LOCATION, internalStoragePath + File.separator + "Paste")
                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
                }
            }
            "S" -> {
                Intent(COPY_BROADCAST_ACTION).apply {
                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    putExtra(PASTE_LOCATION, sdCardPath + File.separator + "Paste")
                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
                }
            }

            else -> Intent("Noop")
        }

        intent.putExtra(COPY_TYPE_EXTRA, MOVE)
//        starting the copy service
        view.context().sendBroadcast(intent)
    }

    fun getTreeUri(context: Context, storageName: String): Uri {
        val sp = context.getSharedPreferences(
            SD_CARD_TREE_URI_SP,
            AppCompatActivity.MODE_PRIVATE
        )
        return sp.getString(TREE_URI_ + storageName, "")!!.toUri()
    }

    fun updateCopyProgress(p: Int) {
        view.updateCopyProgress(p, 10, "", "")
    }

    fun cancelCopy() {
        CopyServices.cancelCopy()
        view.stopCloseCopyScreen()
        stopActionMode()
        supPresenter.loadFiles()
        Toast.makeText(view.context(), "copy done", Toast.LENGTH_SHORT).show()
    }

}