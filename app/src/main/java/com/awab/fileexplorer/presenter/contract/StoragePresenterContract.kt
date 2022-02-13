package com.awab.fileexplorer.presenter.contract

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.PickViewSettingsLayoutBinding
import com.awab.fileexplorer.model.RecentFiles
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.data_models.SelectedItemsDetailsModel
import com.awab.fileexplorer.model.types.FileType
import com.awab.fileexplorer.model.types.MimeType
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.presenter.threads.SelectedFilesDetailsAsyncTask
import com.awab.fileexplorer.view.helper_view.CustomDialog
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File
import java.text.SimpleDateFormat

/**
 * this the presenter class interface
 * the view foreword the user interactions to this class
 * currently it handles all the logic and the data base stuff
 * and it responsible of updating the view
 */
interface StoragePresenterContract {

    // the path of the current storage
    val storagePath: String

    /**
     * the presenter that control the fragments
     */
    var supPresenter: SupPresenter

    /**
     * this indicates when tha action mode is active... so the view show the action mode ui
     */
    var actionModeOn: Boolean

    /**
     * the current view that is linked with this presenter
     * the presenter will update this view
     */
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

    /**
     * save the uir for the sd card to the shared preferences for later us
     */
    fun saveTreeUri(treeUri: Uri)

    /**
     * request the permission to do write operations to the storage
     */
    fun requestPermission()

    /**
     * checks if the return uri that the user has selected is for the sd card or not
     */
    fun isValidTreeUri(treeUri: Uri): Boolean

    /**
     * to check the write permission is granted... if so a dialog will open to continue the presses
     * else the the user will be asked for the permission
     */
    fun confirmCreateFolder() {
        if (!isAuthorized()) {
            requestPermission()
            return
        }
        view.showCreateFolderDialog()
    }

    /**
     * create a new folder in the storage
     */
    fun createFolder(path: String)

    /**
     * to check the write permission is granted... if so a dialog will open to continue the presses
     * else the the user will be asked for the permission
     */
    fun confirmRename() {
        if (!isAuthorized()) {
            requestPermission()
            return
        }
        val item = supPresenter.getSelectedItems()[0]
        view.showRenameDialog(item.path, item.name)
    }

    /**
     * rename the selected file to the new name
     * @param path the path of the selected file
     * @param newName the new name of the file
     */
    fun rename(path: String, newName: String)

    /**
     * to check the write permission is granted... if so a dialog will open to continue the presses
     * else the the user will be asked for the permission
     */
    fun confirmDelete() {
        if (!isAuthorized()) {
            requestPermission()
            return
        }
        view.confirmDelete()
    }

    /**
     * delete all the selected file after the confirmation.
     */
    fun delete()

    /**
     * the "rename" menu item must only be shown when one item is selected
     * this method will decide to show this menu item or not
     * @return true when the "rename" menu item must be shown, false otherwise.
     */
    fun showMIRename(): Boolean {
//        true to show rename
        val count = supPresenter.getSelectedItemCount()
        return count == 1
    }

    /**
     * the "open with" menu item must only be shown when one item is selected and that item must be a file
     * this method will decide to show this menu item or not
     * @return true when the "open with" menu item must be shown, false otherwise.
     */
    fun showMIOpenWith(): Boolean {
//        true to show rename
        val items = supPresenter.getSelectedItems()
        return items.count() == 1 && items[0].type == FileType.FILE
    }

    /**
     * removes the last Breadcrumb item from the view
     */
    fun removeBreadcrumb() {
        view.removeBreadcrumb()
    }

    /**
     * gives the title that must be displayed on the view while the action mode is active
     * the title represent tha number of the selected items
     * @return the title of the action mode, which is the number of selected items
     */
    fun getActionModeTitle(): String {
        val count = supPresenter.getSelectedItemCount()

        return if (count <= 1)
            "$count item Selected"
        else
            "$count items Selected"
    }

    /**
     * select all the items in the list
     */
    fun selectAll() {
        supPresenter.selectAll()
        view.updateActionMode()
    }

    /**
     * it will decide if the action mode should continue or it must be stopped
     * this method check if there is no selected items, if so the action mode must stop
     * @return true if there is no selected items and the action mode must stop, false otherwise
     */
    fun shouldStopActionMode(): Boolean {
        val count = supPresenter.getSelectedItemCount()
        return count <= 0
    }

    /**
     * stop the action mode view state and return the view to the normal state
     */
    fun stopActionMode() {
        actionModeOn = false
        view.stopActionMode()
        supPresenter.stopActionMode()
    }

    /**
     * this call from the action mode menu
     * it will show all the details of the selected items in a dialog
     */
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
            SelectedFilesDetailsAsyncTask(viewHiddenFilesSettings(),
                object : SimpleSuccessAndFailureCallback<SelectedItemsDetailsModel> {
                    override fun onSuccess(data: SelectedItemsDetailsModel) {
                        view.showDetails(data.contains, data.totalSize)
                         view.loadingDialog.dismiss()
                    }

                    override fun onFailure(message: String) {
                        view.showToast(message)
                        view.loadingDialog.dismiss()
                    }
                }).execute(items)
        }
    }

    /**
     * this will get called when a file item is click in the list
     */
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

    /**
     * this will get called when a file item is long clicked in the list
     */
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

    /**
     * it will return an intent to open or to view a media file:Image, video ...
     * @param file the that will be open or viewed
     * @return an intent with ACTION_VIEW and has the data and type of the given file
     */
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
        view.showPickLocation()
//        val selectedItem = supPresenter.getSelectedItems()
//        if (selectedItem.isEmpty())
//            return
//        view.startCopyScreen()
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

//        val intent = when (to) {
//            "I" -> {
//                Intent(COPY_BROADCAST_ACTION).apply {
//                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
//                    //  the copy location
//                    putExtra(PASTE_LOCATION, internalStoragePath + File.separator + "Paste")
//                }
//            }
//            "S" -> {
//                Intent(COPY_BROADCAST_ACTION).apply {
//                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
//                    //  the copy location
//                    putExtra(PASTE_LOCATION, sdCardPath + File.separator + "Paste")
//                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
//                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
//                }
//            }
//
//            else -> Intent("Noop")
//        }
//
//        intent.putExtra(COPY_TYPE_EXTRA, COPY)
////        starting the copy service
//        view.context().sendBroadcast(intent)
    }

    fun move(to: String) {
        val selectedItem = supPresenter.getSelectedItems()
        if (selectedItem.isEmpty())
            return

        val listPath = selectedItem.map { it.path }
        view.openCopyProgress("Moving")

//        val intent = when (to) {
//            "I" -> {
//                Intent(COPY_BROADCAST_ACTION).apply {
//                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
//                    //  the copy location
//                    putExtra(PASTE_LOCATION, internalStoragePath + File.separator + "Paste")
//                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
//                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
//                }
//            }
//            "S" -> {
//                Intent(COPY_BROADCAST_ACTION).apply {
//                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
//                    putExtra(PASTE_LOCATION, sdCardPath + File.separator + "Paste")
//                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
//                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
//                }
//            }
//
//            else -> Intent("Noop")
//        }
//
//        intent.putExtra(COPY_TYPE_EXTRA, MOVE)
////        starting the copy service
//        view.context().sendBroadcast(intent)
    }

    /**
     * it will load the saved tree uri of the sd card from the shared preferences
     * @return the tree uri of the sd card
     */
    fun getTreeUri(context: Context, storageName: String): Uri {
        val sp = context.getSharedPreferences(
            SD_CARD_TREE_URI_SP,
            AppCompatActivity.MODE_PRIVATE
        )
        return sp.getString(TREE_URI_ + storageName, "")!!.toUri()
    }

    fun cancelCopy() {
        CopyServices.cancelCopy()
        view.stopCloseCopyScreen()
        stopActionMode()
        supPresenter.loadFiles()
        Toast.makeText(view.context(), "copy done", Toast.LENGTH_SHORT).show()
    }

    /**
     * it will show a screen for the user to edit the view settings
     */
    fun pickViewSettings() {
        val viewSortBy = viewSortBySettings()
        val viewSortOrder = viewSortOrderSettings()
        val showHiddenFiles = viewHiddenFilesSettings()

        val dialogBinding = PickViewSettingsLayoutBinding.inflate(LayoutInflater.from(view.context()))
        val rgSortingBy: RadioGroup = dialogBinding.rgViewType
        val rgSortingOrder: RadioGroup = dialogBinding.rgViewOrder

        // putting the saved viewing setting data
        when (viewSortBy) {
            SORTING_BY_NAME -> rgSortingBy.check(R.id.rbName)
            SORTING_BY_SIZE -> rgSortingBy.check(R.id.rbSize)
            DEFAULT_SORTING_ARGUMENT -> rgSortingBy.check(R.id.rbDate)

            // sort by name is the default
            else -> rgSortingBy.check(R.id.rbName)
        }
        when (viewSortOrder) {
            SORTING_ORDER_ASC -> rgSortingOrder.check(R.id.rbAscending)
            SORTING_ORDER_DEC -> rgSortingOrder.check(R.id.rbDescending)

            // sort ascending is the default
            else -> rgSortingBy.check(R.id.rbName)
        }
        dialogBinding.btnShowHiddenFiles.isChecked = showHiddenFiles
        val dialog = CustomDialog.makeDialog(view.context(), dialogBinding.root).apply { setTitle("View Settings") }
        view.pickNewViewingSettings(dialog, dialogBinding)
    }

    /**
     * it will return the saved sort argument
     * @return the a string that represent the sort argument
     */
    fun viewSortBySettings(): String? {
        val sp =
            view.context().getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getString(SHARED_PREFERENCES_SORTING_BY, DEFAULT_SORTING_ARGUMENT)
    }

    /**
     * it will return the saved sort order
     * @return the a string that represent the sort order
     */
    fun viewSortOrderSettings(): String? {
        val sp =
            view.context().getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getString(SHARED_PREFERENCES_SORTING_ORDER, DEFAULT_SORTING_ORDER)
    }

    /**
     * it will return the saved setting for showing the hidden file
     * @return true to show the hidden file, false otherwise
     */
    fun viewHiddenFilesSettings(): Boolean {
        val sp =
            view.context().getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, DEFAULT_SHOW_HIDDEN_FILES)
    }

    /**
     * save the view settings to the shared preferences.
     * @param sortBy the sort argument.
     * @param order the sort order.
     * @param showHiddenFiles the hidden files visibility.
     */
    fun saveViewingSettings(sortBy: String, order: String, showHiddenFiles: Boolean) {
        val sharedPreferencesEditor = view.context().getSharedPreferences(
            VIEW_SETTINGS_SHARED_PREFERENCES,
            AppCompatActivity.MODE_PRIVATE
        ).edit()
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_BY, sortBy)
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_ORDER, order)
        sharedPreferencesEditor.putBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, showHiddenFiles)
        sharedPreferencesEditor.apply()
    }

    /**
     * refresh the files list
     */
    fun refreshList() {
        supPresenter.loadFiles()
    }

    fun openWith() {
        val intent = getOpenFileIntent(supPresenter.getSelectedItems()[0])
        val chooserIntent =
            Intent.createChooser(intent, view.context().getString(R.string.open_with_chooser_title))
        view.openFile(chooserIntent)
    }

//    fun getTransferResult(): Intent {
//        val items  = supPresenter.getSelectedItems()
//        val type = COPY
//        val l = ArrayList<String>()
//
//        items.forEach { l.add(it.path) }
//        val bundle = Bundle().apply {
//            putStringArrayList("T_ITEMS",l)
//            putString("T_T",type)
//        }
//
//        return Intent().putExtra("T_DATA", bundle)
//    }
}