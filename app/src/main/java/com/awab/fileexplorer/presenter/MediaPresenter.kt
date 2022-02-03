package com.awab.fileexplorer.presenter

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.net.toUri
import com.awab.fileexplorer.R
import com.awab.fileexplorer.model.RecentFiles
import com.awab.fileexplorer.model.data_models.MediaItemModel
import com.awab.fileexplorer.model.types.MediaCategory
import com.awab.fileexplorer.model.types.MimeType
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.presenter.contract.MediaPresenterContract
import com.awab.fileexplorer.view.callbacks.LoadMediaCallback
import com.awab.fileexplorer.view.contract.MediaView
import com.awab.fileexplorer.view.threads.MediaLoaderWorkerThread
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaPresenter(override val view: MediaView) : MediaPresenterContract {
    private val TAG = "MediaPresenter"

    private val mediaLoaderCallback = object:LoadMediaCallback{
        override fun onSuccess(list: List<MediaItemModel>) {
            view.mediaAdapter.setList(list)
        }

        override fun onFailure(message: String) {
            view.mediaAdapter.setList(listOf())
            view.showToast(message)
        }
    }

    override var actionModeOn: Boolean = false

    override fun loadMedia(intent: Intent) {
        val category = intent.getSerializableExtra(MEDIA_CATEGORY_EXTRA)
        if (category !is MediaCategory){
            view.mediaAdapter.setList(listOf())
            return
        }
        when(category) {
            MediaCategory.IMAGES->{
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                getMediaFiles(contentUri, PROJECTION, null, null)
            }
            MediaCategory.VIDEOS->{
                val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                getMediaFiles(contentUri, PROJECTION, null, null)
            }
            MediaCategory.AUDIO->{
                val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                getMediaFiles(contentUri, PROJECTION, null, null)
            }
            MediaCategory.DOCUMENTS -> {
                val docsContentUri = MediaStore.Files.getContentUri("external")
                val selection = "_data LIKE ? OR _data LIKE ? OR _data LIKE ? OR _data LIKE ? "
                val selectionArgs = arrayOf("%.pdf%", "%.txt%", "%.html%", "%.xml%")
                getMediaFiles(docsContentUri, PROJECTION, selection, selectionArgs)
            }
        }
    }

    override fun getOpenFileIntent(file: MediaItemModel): Intent {
        RecentFiles.recentFilesList.add(file.path)

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, file.type.mimeString)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }

    override fun mediaItemClicked(item: MediaItemModel) {
//        selecting unselecting the item
        if (actionModeOn) {
            processClick(item)
            return
        }

//        opining the item

//        file cant be opened
        if (item.type == MimeType.UNKNOWN) {
            Toast.makeText(view.context(), "unsupported file format", Toast.LENGTH_SHORT).show()
            return
        }
//            opening the file
        view.openFile(getOpenFileIntent(item))
    }

    override fun mediaItemLongClicked(item: MediaItemModel) {
        if (actionModeOn) {
//            handle click while action mode is on
            processClick(item)
            return
        }
        actionModeOn = true
        view.mediaAdapter.selectOrUnselectItem(item)
        view.startActionMode()
    }

    override fun processClick(item: MediaItemModel) {
        view.mediaAdapter.selectOrUnselectItem(item)
        view.updateActionMode()

        val items = view.mediaAdapter.getSelectedItems()
        if (items.isEmpty()) {
//            stop the action mode with the normal flow
            view.pressBack()
        }
    }

    override fun getActionModeTitle(): String {
        val items = view.mediaAdapter.getSelectedItems()
        return items.count().toString()
    }

    override fun stopActionMode() {
//        action mode to false must be first so the adapter work correctly
        actionModeOn = false
        view.stopActionMode()
        view.mediaAdapter.unselectAll()
    }

    override fun oneItemSelected(): Boolean {
        return view.mediaAdapter.getSelectedItems().count() == 1
    }

    override fun showDetails() {
        val items = view.mediaAdapter.getSelectedItems()
        if (items.size == 1) {
            val selectedItem = items[0]
            val name = selectedItem.name
            val path = selectedItem.path
            val size = selectedItem.size
            val date = Date(File(selectedItem.path).lastModified())
            val dateStr = SimpleDateFormat(DATE_FORMAT_PATTERN).format(date)

            view.showDetails(name, path, size, dateStr)
        } else if (items.size > 1) {
            val contains = "${items.size} Files"
            var totalSizeBytes = 0L

            items.forEach {
                totalSizeBytes += File(it.path).length()
            }
            val totalSize = getSize(totalSizeBytes)

            view.showDetails(contains, totalSize)
        }
    }

    override fun selectAll() {
        view.mediaAdapter.selectAll()
        view.updateActionMode()
    }

    override fun openWith() {
//        this only called when one item is selected
        val item = view.mediaAdapter.getSelectedItems()[0]
        val intent = getOpenFileIntent(item)

        val chooserIntent =
            Intent.createChooser(intent, view.context().getString(R.string.open_with_chooser_title))
//            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // to open the media in a new activity
        view.openFile(chooserIntent)
    }

    private fun getMediaFiles(contentUri: Uri, projection:Array<String>?,
                              selection:String?, selectionArgs:Array<String>?){

        //  load the media items
        val query = view.context().contentResolver.query(
            contentUri, projection, selection, selectionArgs,
            "${MediaStore.MediaColumns.DATE_MODIFIED} DESC", null
        )
        //  this is the querying work.. will be done in a worker thread
        val work: (Unit) -> List<MediaItemModel> = {
            val list = mutableListOf<MediaItemModel>()

            query?.let { query ->
                query.use { cursor ->
                    val nameId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val pathId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    val sizeId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

                    while (cursor.moveToNext()) {
                        try {
                            val name = cursor.getString(nameId)
                            val path = cursor.getString(pathId)
                            val size = cursor.getString(sizeId)

                            val file = File(path)
                            list.add(
                                MediaItemModel(
                                    name, path,
                                    getSize(size.toLong()),
                                    getMime(file),
                                    file.toUri()
                                )
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
            }
            list
        }

        // the worker thread that will handle the work and then notify the ui with the work results
        // with the callback
        MediaLoaderWorkerThread(work, mediaLoaderCallback).execute()
    }

}