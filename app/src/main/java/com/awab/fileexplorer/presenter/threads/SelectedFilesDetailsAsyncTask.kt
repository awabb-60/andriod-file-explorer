package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import android.util.Log
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.data_models.MediaItemModel
import com.awab.fileexplorer.model.data_models.SelectedItemsDetailsModel
import com.awab.fileexplorer.model.utils.getContains
import com.awab.fileexplorer.model.utils.getSize
import com.awab.fileexplorer.model.utils.getTotalSize
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import java.io.File

/**
 * this gets all the selected items data from the media items
 */
class SelectedFilesDetailsAsyncTask(private val callBack: SimpleSuccessAndFailureCallback<SelectedItemsDetailsModel>) :
    AsyncTask<List<FileModel>, Unit, SelectedItemsDetailsModel>() {

    override fun doInBackground(vararg params:List<FileModel>): SelectedItemsDetailsModel {
        // getting details for the selected items
        val list = params[0]
        return SelectedItemsDetailsModel(getTotalSize(list), getContains(list))
    }

    override fun onPostExecute(result: SelectedItemsDetailsModel?) {
        if (result != null)
            callBack.onSuccess(result)
        else
            callBack.onFailure("some error occur")
    }
}