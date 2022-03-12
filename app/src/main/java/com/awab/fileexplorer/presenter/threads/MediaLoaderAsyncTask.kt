package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import android.util.Log
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.MediaItemDataModel

class MediaLoaderAsyncTask(
    private val work: (Unit) -> List<MediaItemDataModel>,
    private val callback: SimpleSuccessAndFailureCallback<List<MediaItemDataModel>>
) :
    AsyncTask<Unit, Unit, (List<MediaItemDataModel>)>() {

    override fun doInBackground(vararg params: Unit?): List<MediaItemDataModel> {
        return try {
            //  loading the media files
            work.invoke(Unit)
        } catch (e: Exception) {
            //  some error happen will loading the files
            listOf()
        }
    }

    override fun onPostExecute(result: List<MediaItemDataModel>) {
        if (result.isNotEmpty()) {
            callback.onSuccess(result)
        }else
            callback.onFailure("unable to load media")
        super.onPostExecute(result)
    }

    override fun onCancelled() {
        Log.d("TAG", "onCancelled: noop")
        super.onCancelled()
    }
}