package com.awab.fileexplorer.presenter.contract

import android.content.Intent
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.view.contract.MediaView

interface MediaPresenterContract {

    val view: MediaView

    var actionModeOn: Boolean

    /**
     * this load all the file from the given category and then update the view
     */
    fun loadFiles(intent: Intent)

    /**
     * call when an item is clicked
     */
    fun mediaItemClicked(item: FileDataModel)

    /**
     * call when an item is long clicked
     */
    fun mediaItemLongClicked(item: FileDataModel)

    /**
     * this called while the action mode is on.. to determine what should happened
     * select or un select the item or to stop the action mode if no item is selected
     */
    fun processClick(item: FileDataModel)

    /**
     * this return the title that will show how many items are selected
     */
    fun getActionModeTitle(): String

    /**
     * stop the action mode
     */
    fun stopActionMode()

    /**
     * this return wither the (open with) item menu should be opened
     * @return true if one item is selected false other wise
     */
    fun showMIOpenWith():Boolean

    /**
     * show the information of the selected items
     */
    fun showDetails()

    /**
     * select all the items is the screen
     */
    fun selectAll()

    /**
     * it list all the apps that can open this specific media file,then the user
     * will chose
     */
    fun openWith()

    /**
     * called when the search text changes, it filter the current media items list with the
     * newText and display the search results
     */
    fun searchTextChanged(newText: String)

    /**
     * cancel the running loading operation of the media files
     */
    fun cancelLoadFiles()
}