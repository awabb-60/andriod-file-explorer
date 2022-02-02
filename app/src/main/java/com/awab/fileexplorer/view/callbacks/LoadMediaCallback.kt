package com.awab.fileexplorer.view.callbacks

import com.awab.fileexplorer.model.data_models.MediaItemModel

interface LoadMediaCallback {

    fun onSuccess(list:List<MediaItemModel>)

    fun onFailure(message:String)

}