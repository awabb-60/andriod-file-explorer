package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.view.contract.ISearchFragmentView

interface SearchPresenterContract:SupPresenter {

    /**
     * the view that display the search results
     */
    val view: ISearchFragmentView

    /**
     * the files list that where the search or the filtering will happen
     */
    var searchList:List<FileDataModel>

    override val mainMenuState: Boolean
        get() = false

    /**
     * this filter the search list by the given text
     * and then update the view
     */
    fun onTextChanged(text: String)

    /**
     * this get called after the query of the search list to update the view
     * and be ready to receive search text
     */
    fun isReady(list: List<FileDataModel>)

    /**
     * to cancel the running operation of loading the search files
     */
    fun cancelLoadFiles()
}