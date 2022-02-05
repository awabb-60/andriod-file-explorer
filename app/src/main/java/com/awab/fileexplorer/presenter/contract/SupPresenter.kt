package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.model.data_models.FileModel

/**
 * this is the presenter tha control any sup fragment view
 */
interface SupPresenter {


    /**
     * the main Storage presenter
     */
    val mainStoragePresenter:StoragePresenterContract

    /**
     * this indicates wither the main menu should be hidden or not
     */
    val mainMenuState:Boolean

    /**
     * the action mode state: true==active, false== inactive
     */
    val actionModeOn:Boolean
    get() = mainStoragePresenter.actionModeOn

    /**
     * this used to load the files list in the sup presenter view or to updated
     */
    fun loadFiles()

    /**
     * call back when a file item is clicked in the supPresenter view
     */
    fun onFileClick(file: FileModel)


    /**
     * call back when a file item is long clicked in the supPresenter view
     */
    fun onFileLongClick(file: FileModel)

    /**
     * to select the item if the item is unselected or
     * to unselect the item if the item is selected
     */
    fun selectOrUnSelectItem(file: FileModel)

    /**
     * select all the items in the supPresenter view
     */
    fun selectAll()

    /**
     * return all the selected items in the supPresenter view
     */
    fun getSelectedItems():List<FileModel>

    /**
     * return the number of selected items
     */
    fun getSelectedItemCount():Int

    /**
     * to remove the action mode ui from the the supPresenter view
     */
    fun stopActionMode()
}