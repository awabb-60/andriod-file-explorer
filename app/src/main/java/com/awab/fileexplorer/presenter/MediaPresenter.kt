package com.awab.fileexplorer.presenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.net.toUri
import com.awab.fileexplorer.R
import com.awab.fileexplorer.model.RecentFiles
import com.awab.fileexplorer.model.data_models.MediaItemModel
import com.awab.fileexplorer.model.data_models.SelectedItemsDetailsModel
import com.awab.fileexplorer.model.types.MediaCategory
import com.awab.fileexplorer.model.types.MimeType
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.presenter.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.presenter.contract.MediaPresenterContract
import com.awab.fileexplorer.presenter.threads.SelectedMediaDetailsAsyncTask
import com.awab.fileexplorer.view.contract.MediaView
import com.awab.fileexplorer.presenter.threads.MediaLoaderAsyncTask
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaPresenter(override val view: MediaView) : MediaPresenterContract {
    private val TAG = "MediaPresenter"

    var mediaItemsList = listOf<MediaItemModel>()

    override var actionModeOn: Boolean = false

    override fun loadMedia(intent: Intent) {
        if (mediaItemsList.isNotEmpty()) {
            view.mediaAdapter.setList(mediaItemsList)
            return
        }
        val category = intent.getSerializableExtra(MEDIA_CATEGORY_EXTRA)
        if (category !is MediaCategory) {
            view.mediaAdapter.setList(listOf())
            return
        }
        when (category) {
            MediaCategory.IMAGES -> {
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                getMediaFiles(contentUri, PROJECTION, null, null)
                view.setTitle("Images")
            }
            MediaCategory.VIDEOS -> {
                val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                getMediaFiles(contentUri, PROJECTION, null, null)
                view.setTitle("Videos")
            }
            MediaCategory.AUDIO -> {
                val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                getMediaFiles(contentUri, PROJECTION, null, null)
                view.setTitle("Audio")
            }
            MediaCategory.DOCUMENTS -> {
                val docsContentUri = MediaStore.Files.getContentUri("external")
                val selection = "_data LIKE ? OR _data LIKE ? OR _data LIKE ? OR _data LIKE ? "
                val selectionArgs = arrayOf("%.pdf%", "%.txt%", "%.html%", "%.xml%")
                getMediaFiles(docsContentUri, PROJECTION, selection, selectionArgs)
                view.setTitle("Documents")
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
        // long click a selected item do nothing
        if (item.selected)
            return
        if (actionModeOn) {
            // handle click while action mode is on
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
            view.finishActionMode()
        }
    }

    override fun getActionModeTitle(): String {
        val count = view.mediaAdapter.getSelectedItems().count()

        return if (count <= 1)
            "$count item Selected"
        else
            "$count items Selected"
    }

    override fun stopActionMode() {
//        action mode to false must be first so the adapter work correctly
        actionModeOn = false
        view.stopActionMode()
        view.mediaAdapter.unselectAll()
    }

    override fun showMIOpenWith(): Boolean {
        return view.mediaAdapter.getSelectedItems().count() == 1
    }

    override fun showDetails() {
        val items = view.mediaAdapter.getSelectedItems()
        when (items.size) {
            1 -> {
                val selectedItem = items[0]
                val name = selectedItem.name
                val path = selectedItem.path
                val size = selectedItem.size
                val date = Date(File(selectedItem.path).lastModified())
                val dateStr = SimpleDateFormat(DATE_FORMAT_PATTERN).format(date)

                view.showDetails(name, path, size, dateStr)
            }
            in 1 until 1000 -> {
                // getting the details for many items
                val contains = "${items.size} Files"

                var totalSizeBytes = 0L
                items.forEach {
                    totalSizeBytes += File(it.path).length()
                }
                val totalSize = getSize(totalSizeBytes)

                view.showDetails(contains, totalSize)
            }
            else -> {
                // loading large items in background thread
                view.loadingDialog.show()
                SelectedMediaDetailsAsyncTask(object : SimpleSuccessAndFailureCallback<SelectedItemsDetailsModel> {
                    override fun onSuccess(data: SelectedItemsDetailsModel) {
                        view.showDetails(data.contains, data.totalSize)
                        view.loadingDialog.dismiss()
                    }

                    override fun onFailure(message: String) {
                        view.loadingDialog.dismiss()
                        view.showToast("some error occur")
                    }
                }).execute(items)
            }
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

    fun showHiddenFiles():Boolean{
        val sp = view.context().getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return sp.getBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, false)
    }

    private fun getMediaFiles(
        contentUri: Uri, projection: Array<String>?,
        selection: String?, selectionArgs: Array<String>?
    ) {
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
        MediaLoaderAsyncTask(work, object : SimpleSuccessAndFailureCallback<List<MediaItemModel>> {
            override fun onSuccess(data: List<MediaItemModel>) {
                mediaItemsList = if (!showHiddenFiles()) // filtering the hidden files
                    data.filter { !it.name.startsWith('.') }
                else
                    data
                view.mediaAdapter.setList(mediaItemsList)
            }

            override fun onFailure(message: String) {
                view.mediaAdapter.setList(listOf())
                view.showToast(message)
            }
        }).execute()
    }

    override fun searchTextChanged(newText: String) {
        val query = mediaItemsList.filter { it.name.lowercase().contains(newText.lowercase()) }
        view.mediaAdapter.searchText = newText
        view.mediaAdapter.setList(query)
    }
}