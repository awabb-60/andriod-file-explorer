package com.awab.fileexplorer.presenter

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.awab.fileexplorer.R
import com.awab.fileexplorer.model.MainStorageModel
import com.awab.fileexplorer.presenter.contract.MediaPresenterContract
import com.awab.fileexplorer.utils.DATE_FORMAT_PATTERN
import com.awab.fileexplorer.utils.MEDIA_CATEGORY_EXTRA
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.FilesDetailsDataModel
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.types.MediaCategory
import com.awab.fileexplorer.utils.data.types.MimeType
import com.awab.fileexplorer.utils.data.types.QuickAccessFileType
import com.awab.fileexplorer.utils.getOpenFileIntent
import com.awab.fileexplorer.utils.getSize
import com.awab.fileexplorer.view.contract.MediaView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaPresenter(override val view: MediaView) : MediaPresenterContract {

    var mediaItemsList = listOf<FileDataModel>()

    override var actionModeOn: Boolean = false

    val model = MainStorageModel(view.context())

    override fun loadFiles(intent: Intent) {
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
                getMediaFiles(contentUri, null, null)
                view.setTitle("Images")
            }
            MediaCategory.VIDEOS -> {
                val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                getMediaFiles(contentUri, null, null)
                view.setTitle("Videos")
            }
            MediaCategory.AUDIO -> {
                val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                getMediaFiles(contentUri, null, null)
                view.setTitle("Audio")
            }
            MediaCategory.DOCUMENTS -> {
                val docsContentUri = MediaStore.Files.getContentUri("external")
                val selection = "_data LIKE ? OR _data LIKE ? OR _data LIKE ? OR _data LIKE ? "
                val selectionArgs = arrayOf("%.pdf%", "%.txt%", "%.html%", "%.xml%")
                getMediaFiles(docsContentUri, selection, selectionArgs)
                view.setTitle("Documents")
            }
        }
    }

    override fun mediaFileClicked(file: FileDataModel) {
//        selecting unselecting the item
        if (actionModeOn) {
            processClick(file)
            return
        }

//        opining the item
//        file cant be opened
        if (file.mimeType == MimeType.UNKNOWN) {
            Toast.makeText(view.context(), "unsupported file format", Toast.LENGTH_SHORT).show()
            return
        }
//            opening the file
        view.openFile(getOpenFileIntent(file))
        model.saveToQuickAccessFiles(listOf(QuickAccessFileDataModel(file.path, QuickAccessFileType.RECENT)))
    }

    override fun mediaItemLongClicked(item: FileDataModel) {
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

    override fun processClick(item: FileDataModel) {
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
                val dateStr = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US).format(date)

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
                model.getSelectedMediaDetails(
                    items,
                    object : SimpleSuccessAndFailureCallback<FilesDetailsDataModel> {
                        override fun onSuccess(data: FilesDetailsDataModel) {
                            view.loadingDialog.dismiss()
                            view.showDetails(data.contains, data.totalSize)
                        }

                        override fun onFailure(message: String) {
                            view.loadingDialog.dismiss()
                            view.showToast("some error occur")
                        }
                    })
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

    private fun getMediaFiles(
        contentUri: Uri, selection: String?, selectionArgs: Array<String>?
    ) {
        val mediaFilesProjection = arrayOf(
            MediaStore.MediaColumns.DATA,
        )
        model.queryFiles(
            contentUri,
            mediaFilesProjection,
            selection,
            selectionArgs,
            object : SimpleSuccessAndFailureCallback<List<FileDataModel>> {
                override fun onSuccess(data: List<FileDataModel>) {
                    mediaItemsList = data
                    view.mediaAdapter.setList(mediaItemsList)
                }

                override fun onFailure(message: String) {
                    view.mediaAdapter.setList(listOf())
                    view.showToast(message)
                }
            })
    }

    override fun cancelLoadFiles() {
        model.cancelQueryFiles()
    }

    override fun searchTextChanged(newText: String) {
        val query = mediaItemsList.filter { it.name.lowercase().contains(newText.lowercase()) }
        view.mediaAdapter.searchText = newText
        view.mediaAdapter.setList(query)
    }
}