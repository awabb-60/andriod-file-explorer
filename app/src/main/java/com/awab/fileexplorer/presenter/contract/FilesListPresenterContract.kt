package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.model.MainStorageModel
import com.awab.fileexplorer.view.contract.IFileFragmentView

interface FilesListPresenterContract:SupPresenter {

    val view: IFileFragmentView

    val model: MainStorageModel

    override val mainMenuState: Boolean
        get() = true

    /**
     * removing the breadcrumb of this fragment when
     */
    fun removeBreadcrumb()
}