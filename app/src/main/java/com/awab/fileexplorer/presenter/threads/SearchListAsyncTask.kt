package com.awab.fileexplorer.presenter.threads

import android.content.ContentResolver
import android.os.AsyncTask
import android.provider.MediaStore
import com.awab.fileexplorer.model.utils.makeFileModels
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import java.io.File


class SearchListAsyncTask(
    private val folderPath: String,
    private val contentResolver: ContentResolver,
    private val callback: SimpleSuccessAndFailureCallback<List<FileDataModel>>
) :
    AsyncTask<Unit, Unit, List<FileDataModel>>() {

    override fun doInBackground(vararg params: Unit?): List<FileDataModel> {
//            val folder = File(folderPath)
//            val allFiles = getInnerFiles(folder, false)
        return try {
            val query = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                null, "_data Like ?",
                arrayOf("%$folderPath%"), null
            )

            val allFiles = mutableListOf<File>()
            query?.let { cursor ->
                cursor.use {
                    val pathId = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    while (it.moveToNext()) {
                        val path = it.getString(pathId)
                        allFiles.add(File(path))
                    }
                }
            }
            makeFileModels(allFiles)
        } catch (e: Exception) {
            listOf()
        }
    }

    override fun onPostExecute(result: List<FileDataModel>) {
        if (result.isEmpty())
            callback.onFailure("unable to load files for search")
        else
            callback.onSuccess(result)
    }

    override fun onCancelled() {
        callback.onFailure("unable to load files for search")
    }
}