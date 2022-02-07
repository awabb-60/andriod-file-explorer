package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import android.util.Log
import com.awab.fileexplorer.model.data_models.MediaItemModel
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback

class MediaLoaderAsyncTask(
    private val work: (Unit) -> List<MediaItemModel>,
    private val callback: SimpleSuccessAndFailureCallback<List<MediaItemModel>>
) :
    AsyncTask<Unit, Unit, (List<MediaItemModel>)>() {

    override fun doInBackground(vararg params: Unit?): List<MediaItemModel> {
        return try {
            //  loading the media files
            work.invoke(Unit)
        } catch (e: Exception) {
            //  some error happen will loading the files
            listOf()
        }
    }

    override fun onPostExecute(result: List<MediaItemModel>) {
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