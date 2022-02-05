package com.awab.fileexplorer.presenter.threads

import android.content.ContentResolver
import android.os.AsyncTask
import android.provider.MediaStore
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.utils.makeFileModels
import com.awab.fileexplorer.presenter.SearchFragmentPresenter
import java.io.File


class SearchListAsyncTask(
    private val presenter: SearchFragmentPresenter,
    private val folderPath: String,
    private val contentResolver: ContentResolver
) :
    AsyncTask<Unit, Unit, List<FileModel>>() {

    override fun doInBackground(vararg params: Unit?): List<FileModel> {
//            val folder = File(folderPath)
//            val allFiles = getInnerFiles(folder, false)

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
        return makeFileModels(allFiles)
    }

    override fun onPostExecute(result: List<FileModel>) {
        presenter.isReady(result)
        super.onPostExecute(result)
    }
}