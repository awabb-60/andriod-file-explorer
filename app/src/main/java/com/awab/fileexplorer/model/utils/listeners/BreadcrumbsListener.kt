package com.awab.fileexplorer.model.utils.listeners

import com.awab.fileexplorer.model.data_models.BreadcrumbsModel

interface BreadcrumbsListener {
    fun onBreadcrumbsItemClicked(item: BreadcrumbsModel)
}