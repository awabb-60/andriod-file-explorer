package com.awab.fileexplorer.presenter.contract

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.PickViewSettingsLayoutBinding
import com.awab.fileexplorer.model.contrancts.StorageModel
import com.awab.fileexplorer.model.utils.getFolderSizeBytes
import com.awab.fileexplorer.model.utils.getSize
import com.awab.fileexplorer.presenter.SdCardPresenterSAF
import com.awab.fileexplorer.presenter.threads.SelectedFilesDetailsAsyncTask
import com.awab.fileexplorer.utils.*
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.SelectedItemsDetailsDataModel
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.utils.data.data_models.TransferInfoDataModel
import com.awab.fileexplorer.utils.data.types.FileType
import com.awab.fileexplorer.utils.data.types.MimeType
import com.awab.fileexplorer.utils.data.types.StorageType
import com.awab.fileexplorer.utils.data.types.TransferAction
import com.awab.fileexplorer.utils.transfer_utils.TransferBroadCast
import com.awab.fileexplorer.view.contract.StorageView
import com.awab.fileexplorer.view.custom_views.CustomDialog
import com.awab.fileexplorer.view.custom_views.PickPasteLocationDialogFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * this the presenter class interface
 * the view foreword the user interactions to this class
 * currently it handles all the logic and the data base stuff
 * and it responsible of updating the view
 */
interface StoragePresenterContract {

    /**
     *  the path of the current storage
     */
    val storagePath: String

    /**
     *  the name of the current storage
     */
    val storageName: String

    /**
     * used to check that uri that was retained for the SAF Picker
     */
    var targetedUnAuthorizedSDCardName: String

    /**
     * the presenter that control the fragments
     */
    var supPresenter: SupPresenter


    /**
     * this indicates when tha action mode is active... so the view show the action mode ui
     */
    var actionModeOn: Boolean

    /**
     * the current view that is linked to this presenter
     * the presenter will update this view
     */
    val view: StorageView

    /**
     * the model that will handle all the data logic
     */
    val model: StorageModel

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
    fun saveTreeUri(treeUri: Uri) {
        model.saveTreeUri(treeUri, targetedUnAuthorizedSDCardName)
    }

    /**
     * request the permission to do write operations to the storage
     */
    fun requestPermission()

    /**
     * checks if the return uri that the user has selected is for the sd card or not
     */
    fun isValidTreeUri(treeUri: Uri): Boolean {
        DocumentFile.fromTreeUri(view.context(), treeUri)?.name?.let { sdCardName ->
            return sdCardName == targetedUnAuthorizedSDCardName
        }
        return false
    }

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
     * @param showMessages the show toast messages that related to the operation
     */
    fun delete(showMessages:Boolean = true)

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
            val date = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US).format(item.date)
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
            SelectedFilesDetailsAsyncTask(model.viewHiddenFilesSettings(),
                object : SimpleSuccessAndFailureCallback<SelectedItemsDetailsDataModel> {
                    override fun onSuccess(data: SelectedItemsDetailsDataModel) {
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
    fun onFileClicked(file: FileDataModel) {
        // selecting unselecting the item
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
    fun onFileLongClicked(file: FileDataModel) {
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
    fun getOpenFileIntent(file: FileDataModel): Intent {
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

    fun pickTransferLocation(transferAction: TransferAction) {
        val storagesList = view.intent().getParcelableArrayListExtra<StorageDataModel>(STORAGES_LIST_EXTRA)

        storagesList?.let {
            val pickLocationFragment = PickPasteLocationDialogFragment.newInstance(
                it.toArray(arrayOf<StorageDataModel>()),
                transferAction
            )

            pickLocationFragment.isCancelable = false
            view.showPickLocation(pickLocationFragment)
        }
    }

    // to copy or move to the sd card or the internal storage, you have to have a folder
    // named (Paste) in location you try to copy to.
    fun transfer(folderLocation: String, storage: StorageDataModel, action: TransferAction) {
        // authorizing the current storage
        if (!isAuthorized()) {
            return
        }

        // if the user is transferring to the st card and its not authorized
        if (storage.storageType == StorageType.SDCARD) {
            val helperPresenter = SdCardPresenterSAF(view, storage.path)
            if (!helperPresenter.isAuthorized()) {
                view.showToast("authorize the sd card first")

                // set the uri target name
                targetedUnAuthorizedSDCardName = File(storage.path).name
                helperPresenter.requestPermission()
                return
            }
        }

        val selectedItem = supPresenter.getSelectedItems()
        if (selectedItem.isEmpty())
            return

        val listPath = selectedItem.map { it.path }
        view.openProgressScreen(action.name)

        val intent = when (storage.storageType) {
            StorageType.INTERNAL -> {
                Intent(TransferBroadCast.ACTION).apply {
                    putExtra(TRANSFER_FILES_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    //  the copy location
                    putExtra(PASTE_LOCATION_PATH_EXTRA, folderLocation)
                }
            }

            // transfer to sd card
            StorageType.SDCARD -> {
                Intent(TransferBroadCast.ACTION).apply {
                    putExtra(TRANSFER_FILES_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    // the location where the files will go to
                    putExtra(PASTE_LOCATION_PATH_EXTRA, folderLocation)

                    // information to write to the sd card : tree uri

                    putExtra(TREE_URI_FOR_TRANSFER_EXTRA, model.getTreeUri(File(storage.path).name))

                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, storagePath)
                }
            }
            else -> Intent("Noop")
        }

        // the transfer action
        intent.putExtra(TRANSFER_ACTION_EXTRA, action as Parcelable)
        // the paste location storage model
        intent.putExtra(PASTE_LOCATION_STORAGE_MODEL_EXTRA, storage)

        // starting the copy service
        view.context().sendBroadcast(intent)
    }

    fun transferFinished(intent: Intent?) {
        view.closeProgressScreen()
        // if the transfer action was move and the all files move successfully
        val info = intent?.getParcelableExtra<TransferInfoDataModel>(TRANSFER_INFO_EXTRA)

        info?:return

        if (info.action == TransferAction.MOVE && info.wasSuccessful) {
            // deleting the moved files
            delete(false)
            view.showToast("Files Moved")
        } else if (info.action == TransferAction.COPY && info.wasSuccessful) {
            stopActionMode()
            supPresenter.loadFiles()
            view.showToast("Files Coped")
        }

        // show the transfer messages
        info.messages.forEach {
            view.showToast(it)
        }
    }

    /**
     * it will show a screen for the user to edit the view settings
     */
    fun pickViewSettings() {
        val viewSortBy = model.viewSortBySettings()
        val viewSortOrder = model.viewSortOrderSettings()
        val showHiddenFiles = model.viewHiddenFilesSettings()

        val dialogBinding = PickViewSettingsLayoutBinding.inflate(LayoutInflater.from(view.context()))
        val rgSortingBy: RadioGroup = dialogBinding.rgViewType
        val rgSortingOrder: RadioGroup = dialogBinding.rgViewOrder

        // putting the saved viewing setting data
        when (viewSortBy) {
            SORTING_BY_NAME -> rgSortingBy.check(R.id.rbName)
            SORTING_BY_SIZE -> rgSortingBy.check(R.id.rbSize)
            SORTING_BY_DATE -> rgSortingBy.check(R.id.rbDate)
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
        val dialog =
            CustomDialog.makeDialog(view.context(), dialogBinding.root).apply { setTitle("View Settings") }
        view.pickNewViewingSettings(dialog, dialogBinding)
    }

    /**
     * save the view settings to the shared preferences.
     * @param sortBy the sort argument.
     * @param order the sort order.
     * @param showHiddenFiles the hidden files visibility.
     */
    fun saveViewingSettings(sortBy: String, order: String, showHiddenFiles: Boolean) {
        model.saveViewingSettings(sortBy, order, showHiddenFiles)
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

    fun pinFile() {
        model.saveToPinedFiles(supPresenter.getSelectedItems())
    }
}