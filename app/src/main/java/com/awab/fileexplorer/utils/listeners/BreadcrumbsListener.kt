package com.awab.fileexplorer.utils.listeners

import com.awab.fileexplorer.utils.data.data_models.BreadcrumbsDataModel

interface BreadcrumbsListener {
    fun onBreadcrumbsItemClicked(item: BreadcrumbsDataModel)
}