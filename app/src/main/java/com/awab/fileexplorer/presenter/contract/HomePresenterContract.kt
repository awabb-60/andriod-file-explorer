package com.awab.fileexplorer.presenter.contract

import com.awab.fileexplorer.model.data_models.StorageModel
import com.awab.fileexplorer.view.contract.HomeView

interface HomePresenterContract {

    fun openStorage(it: StorageModel)

    fun mediaItemClicked(id: Int)

    val view: HomeView
}