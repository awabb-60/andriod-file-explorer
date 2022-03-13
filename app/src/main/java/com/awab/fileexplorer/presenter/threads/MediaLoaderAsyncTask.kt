package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import android.util.Log
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel

class MediaLoaderAsyncTask(
    private val work: (Unit) -> List<FileDataModel>,
    private val callback: SimpleSuccessAndFailureCallback<List<FileDataModel>>
) :
    AsyncTask<Unit, Unit, (List<FileDataModel>)>() {

    override fun doInBackground(vararg params: Unit?): List<FileDataModel> {
        return try {
            //  loading the media files
            work.invoke(Unit)
        } catch (e: Exception) {
            //  some error happen will loading the files
            listOf()
        }
    }

    override fun onPostExecute(result: List<FileDataModel>) {
        if (result.isNotEmpty()) {
            callback.onSuccess(result)
        } else
            callback.onFailure("unable to load media")
        super.onPostExecute(result)
    }

    override fun onCancelled() {
        Log.d("TAG", "onCancelled: noop")
        super.onCancelled()
    }
}