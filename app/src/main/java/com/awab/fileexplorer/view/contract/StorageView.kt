package com.awab.fileexplorer.view.contract

import android.content.Context
import android.content.Intent
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract

interface StorageView {

    val presenter:StoragePresenterContract

    fun context(): Context

    /**
     * get the presenter that control this view
     */
    fun presenter(): StoragePresenterContract {
        return presenter
    }

    /**
     * this opens the media file that in the intent
     */
    fun openFile(intent:Intent)

    /**
     * this open the folder with the given path
     */
    fun navigateToFolder(name:String, path:String)

    fun onFileClickFromSerach(file: FileModel)

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
     * to ask the user to confirm deleting the selected items
     */
    fun confirmDelete()

    /**
     * to ask the user to chose the new name to the renamed item
     */
    fun showRenameDialog(path:String, currentName:String)

    /**
     * this is when one item is selected
     */
    fun showItemDetails(name:String, lastModified:String, size:String, path:String)

    /**
     * this is when many items are selected
     */
    fun showItemsDetails(contains:String, totalSize:String)

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