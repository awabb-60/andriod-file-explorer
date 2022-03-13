package com.awab.fileexplorer.presenter.threads

import android.os.AsyncTask
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel

/**
 * this handle the deletion of any file or folder in the sd card and the devices that has to us
 * SAF the to write to the sd card level 19 - level 30
 */
class DeleteFromSdCardAsyncTask(
    private val parentFolder: DocumentFile,
    private val callback: SimpleSuccessAndFailureCallback<Boolean>
) : AsyncTask<List<FileDataModel>, String, Boolean>() {

    override fun doInBackground(vararg params: List<FileDataModel>?): Boolean {
        var allFilesDeleted = true
        val targetedFiles = params[0]

        //so the loading looks nice
        Thread.sleep(200L)
        //  deleting all the targeted files
        targetedFiles?.forEach {
            try {
                val success = parentFolder.findFile(it.name)?.delete()
                // some error occur... the file was node found or couldn't delete this file
                if (success == null || !success) {
                    // the failure message
                    publishProgress("error deleting ${it.name}")
                    allFilesDeleted = false
                }
            } catch (e: Exception) {
                allFilesDeleted = false
                publishProgress("error deleting ${it.name}")
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