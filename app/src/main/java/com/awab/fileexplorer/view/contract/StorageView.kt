package com.awab.fileexplorer.view.contract

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.awab.fileexplorer.databinding.PickViewSettingsLayoutBinding
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract

interface StorageView {

    /**
     * get the presenter that control this view
     */
    val presenter:StoragePresenterContract

    /**
     * it used to tell the view to show the main menu or not
     */
    var showMenu:Boolean

    /**
     * the dialog that indicate that the screen is loading
     */
    val loadingDialog: AlertDialog

    fun context(): Context

    /**
     * showing toast message on the screen
     */
    fun showToast(message:String)

    /**
     * this opens the media file that in the intent
     */
    fun openFile(intent:Intent)

    /**
     * this open the folder with the given path
     */
    fun navigateToFolder(name:String, path:String)

    /**
     * to update the menu
     */
    fun updateMenu()

    /**
     * removes the breadcrumb item when the navigating out from the file
     */
    fun removeBreadcrumb()

    /**
     * to open the files picker and select the correct sd card storage uri
     */
    fun openAuthorizationPicker(intent: Intent, requestCode:Int)

    /**
     * to put the screen into action mode when selecting items
     */
    fun startActionMode()

    /**
     * to update the action mode title and menu
     */
    fun updateActionMode()

    /**
     * closing the action mode
     */
    fun stopActionMode()

    /**
     * to ask the user to chose the new name to the new folder
     */
    fun showCreateFolderDialog()

    /**
     * to ask the user to chose the new name to the renamed item
     */
    fun showRenameDialog(path:String, currentName:String)

    /**
     * to ask the user to confirm deleting the selected items
     */
    fun confirmDelete()

    /**
     * this is when one item is selected
     */
    fun showDetails(name:String, lastModified:String, size:String, path:String)

    /**
     * this is when many items are selected
     */
    fun showDetails(contains:String, totalSize:String)

    /**
     * show a dialog to let the user pick new viewing settings
     */
    fun pickNewViewingSettings(dialog: AlertDialog, dialogBinding: PickViewSettingsLayoutBinding)

    /**
     * to pick the paste location of the copy items
     */
    fun startCopyScreen()

    /**
     * to pick the paste location of the move items
     */
    fun startMoveScreen()

    /**
     * this open a dialog that will show the copying/ moving progress
     */
    fun openCopyProgress(action:String)

    /**
     * this update the progress dialog with the copy/move progress
     */
    fun updateCopyProgress(p:Int,max:Int, leftDoneText:String, name:String)

    /**
     * to cancel any ui that has to do with copying/moving
     */
    fun stopCloseCopyScreen()
}