package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.view.contract.IFileFragmentView

interface FilesListPresenterContract:SupPresenter {

    val view: IFileFragmentView

    override val mainMenuState: Boolean
        get() = true

    /**
     * removing the breadcrumb of this fragment when
     */
    fun removeBreadcrumb()
}