package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import com.awab.fileexplorer.model.utils.getContains
import com.awab.fileexplorer.model.utils.getTotalSize
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.SelectedItemsDetailsDataModel

/**
 * this gets all the selected items data
 */
class SelectedFilesDetailsAsyncTask(
    private val countHiddenFiles: Boolean,
    private val callBack: SimpleSuccessAndFailureCallback<SelectedItemsDetailsDataModel>
) :
    AsyncTask<List<FileDataModel>, Unit, SelectedItemsDetailsDataModel>() {

    override fun doInBackground(vararg params: List<FileDataModel>): SelectedItemsDetailsDataModel {
        // getting details for the selected items
        val list = params[0]
        return SelectedItemsDetailsDataModel(getTotalSize(list), getContains(list, countHiddenFiles))
    }

    override fun onPostExecute(result: SelectedItemsDetailsDataModel?) {
        if (result != null)
            callBack.onSuccess(result)
        else
            callBack.onFailure("some error occur")
    }
}