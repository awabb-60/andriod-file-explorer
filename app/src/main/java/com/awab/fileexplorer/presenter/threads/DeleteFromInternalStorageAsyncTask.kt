package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import com.awab.fileexplorer.model.utils.deleteFileIO
import com.awab.fileexplorer.model.utils.deleteFolderIO
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.types.FileType

/**
 * this handle the deletion of any file or folder in the sd card and the devices that has to us
 * SAF the to write to the sd card level 19 - level 30
 */
class DeleteFromInternalStorageAsyncTask(
    private val callback: SimpleSuccessAndFailureCallback<Boolean>
) : AsyncTask<List<FileDataModel>, String, Boolean>() {

    override fun doInBackground(vararg params: List<FileDataModel>?): Boolean {
        // so the loading screen looks good
        Thread.sleep(200)
        var allFilesDeleted = true
        val list = params[0]
        list?.forEach {
            try {
                if (it.type == FileType.DIRECTORY)
                    deleteFolderIO(it.path)
                else
                    deleteFileIO(it.path)
            } catch (e: Exception) {
                onProgressUpdate("unable to delete ${it.name}")
                allFilesDeleted = false
            }
        }
        return allFilesDeleted
    }

    override fun onPostExecute(result: Boolean?) {
        if (result != null)
            callback.onSuccess(result)
        super.onPostExecute(result)
    }

    override fun onProgressUpdate(vararg values: String?) {
        values[0]?.let { callback.onFailure(it) }
    }

    override fun onCancelled() {
        callback.onSuccess(false)
        super.onCancelled()
    }
}