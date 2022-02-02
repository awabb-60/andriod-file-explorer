package com.awab.fileexplorer.view.threads

import android.os.AsyncTask
import com.awab.fileexplorer.model.data_models.MediaItemModel
import com.awab.fileexplorer.view.callbacks.LoadMediaCallback

class MediaLoaderWorkerThread(
    private val work: (Unit) -> List<MediaItemModel>,
    private val callback: LoadMediaCallback
) :
    AsyncTask<Unit, Unit, (List<MediaItemModel>)>() {

    override fun doInBackground(vararg params: Unit?): List<MediaItemModel> {
        return try {
            //  loading the media files
            val list = work.invoke(Unit)
            list
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
}