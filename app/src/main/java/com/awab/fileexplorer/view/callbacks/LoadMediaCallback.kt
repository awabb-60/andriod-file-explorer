package com.awab.fileexplorer.view.callbacks

import com.awab.fileexplorer.utils.data.data_models.MediaItemDataModel

interface LoadMediaCallback {

    fun onSuccess(list:List<MediaItemDataModel>)

    fun onFailure(message:String)

}