package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import com.awab.fileexplorer.model.utils.getSize
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.SelectedItemsDetailsDataModel
import java.io.File

/**
 * this gets all the selected items data from the media items
 */
class SelectedMediaDetailsAsyncTask(private val callBack: SimpleSuccessAndFailureCallback<SelectedItemsDetailsDataModel>) :
    AsyncTask<List<FileDataModel>, Unit, SelectedItemsDetailsDataModel>() {

    override fun doInBackground(vararg params: List<FileDataModel>): SelectedItemsDetailsDataModel {
        // getting details for the selected items
        val list = params[0]

        val contains = "${list.size} Files"

        var totalSizeBytes = 0L

        list.forEach {
            totalSizeBytes += File(it.path).length()
        }
        val totalSize = getSize(totalSizeBytes)

        return SelectedItemsDetailsDataModel(totalSize, contains)
    }

    override fun onPostExecute(result: SelectedItemsDetailsDataModel?) {
        if (result != null)
            callBack.onSuccess(result)
        else
            callBack.onFailure("some error occur")
    }
}