package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import android.util.Log
import com.awab.fileexplorer.model.data_models.MediaItemModel
import com.awab.fileexplorer.model.data_models.SelectedItemsDetailsModel
import com.awab.fileexplorer.model.utils.getSize
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import java.io.File

/**
 * this gets all the selected items data from the media items
 */
class SelectedMediaDetailsAsyncTask(private val callBack: SimpleSuccessAndFailureCallback<SelectedItemsDetailsModel>) :
    AsyncTask<List<MediaItemModel>, Unit, SelectedItemsDetailsModel>() {

    override fun doInBackground(vararg params:List<MediaItemModel>): SelectedItemsDetailsModel {
        // getting details for the selected items
        val list = params[0]

        val contains = "${list.size} Files"

        var totalSizeBytes = 0L

        list.forEach {
            totalSizeBytes += File(it.path).length()
        }
        val totalSize = getSize(totalSizeBytes)

        return SelectedItemsDetailsModel(totalSize, contains)
    }

    override fun onPostExecute(result: SelectedItemsDetailsModel?) {
        if (result != null)
            callBack.onSuccess(result)
        else
            callBack.onFailure("some error occur")
    }
}