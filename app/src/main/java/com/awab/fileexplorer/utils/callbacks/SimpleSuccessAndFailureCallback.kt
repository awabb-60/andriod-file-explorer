package com.awab.fileexplorer.utils.callbacks

/**
 * this used to get the data from the worker tasks
 */
interface SimpleSuccessAndFailureCallback<dataType> {

    fun onSuccess(data:dataType)

    fun onFailure(message:String)
}