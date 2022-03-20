package com.awab.fileexplorer.presenter

import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.awab.fileexplorer.R
import com.awab.fileexplorer.model.MainStorageModel
import com.awab.fileexplorer.presenter.contract.HomePresenterContract
import com.awab.fileexplorer.utils.*
import com.awab.fileexplorer.utils.callbacks.SimpleSuccessAndFailureCallback
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.utils.data.types.FileType
import com.awab.fileexplorer.utils.data.types.MediaCategory
import com.awab.fileexplorer.utils.data.types.QuickAccessFileType
import com.awab.fileexplorer.utils.data.types.StorageType
import com.awab.fileexplorer.view.MediaActivity
import com.awab.fileexplorer.view.StorageActivity
import com.awab.fileexplorer.view.contract.HomeView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomePresenter(override val view: HomeView) : HomePresenterContract {

    private val TAG = "HomePresenter"

    private val storages = ArrayList<Parcelable>()

    override val model = MainStorageModel(view.context())

    override var quickAccessInEditMode: Boolean = false

    override fun openStorage(it: StorageDataModel) {
        getOenStorageIntent(it)?.let { view.openActivity(it) }
    }

    private fun getOenStorageIntent(storage: StorageDataModel): Intent? {
        if (!allPermissionsGranted(view.context(), INTERNAL_STORAGE_REQUIRED_PERMISSIONS)) {
            view.checkForPermissions()
            return null
        }
        return Intent(view.context(), StorageActivity::class.java).apply {
            putExtra(STORAGE_PATH_EXTRA, storage.path)
            putExtra(STORAGE_DISPLAY_NAME_EXTRA, storage.name)
            // the type that will determine the presenter
            putExtra(STORAGE_TYPE_EXTRA, storage.storageType)
            putExtra(STORAGES_LIST_EXTRA, storages)
        }
    }

    override fun mediaItemClicked(id: Int) {
        val mediaIntent = Intent(view.context(), MediaActivity::class.java)

        // the ype o media that will be presented
        val type = when (id) {
            R.id.btnMediaImages -> {
                MediaCategory.IMAGES
            }
            R.id.btnMediaVideo -> {
                MediaCategory.VIDEOS
            }
            R.id.btnMediaAudio -> {
                MediaCategory.AUDIO
            }
            R.id.btnMediaDocs -> {
                MediaCategory.DOCUMENTS
            }
            else -> return
        }

        // open the media activity
        mediaIntent.putExtra(MEDIA_CATEGORY_EXTRA, type)
        view.openActivity(mediaIntent)
    }

    private fun makeStorageModel(name: String, file: File, type: StorageType): StorageDataModel? {
        return try {
            val freeSize = getSize(file.freeSpace)
            val totalSize = getSize(file.totalSpace)
            val displaySize = "$freeSize free of $totalSize"
            StorageDataModel(
                name = name,
                size = displaySize,
                freeSizeBytes = file.freeSpace,
                totalSizeBytes = file.totalSpace,
                path = file.absolutePath,
                storageType = type
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun loadStorages() {
        val storagesPaths = ContextCompat.getExternalFilesDirs(view.context(), null)

        // this only get the storage path of the internal storage and sd Card
        val storages = storagesPaths.map {
            it?.parentFile?.parentFile?.parentFile?.parentFile
        }

        val list = mutableListOf<StorageDataModel>()

        // the internal storage will be at the first
        val internalDir = storages[0]
        if (internalDir != null)
            makeStorageModel(
                INTERNAL_STORAGE_DISPLAY_NAME,
                internalDir,
                StorageType.INTERNAL
            )?.let { list.add(it) }

//        making the sd card modes
        if (storages.size == 1)
            return

        val sdCardDir = storages[1]
        if (sdCardDir != null)
            makeStorageModel(EXTERNAL_SDCARD_DISPLAY_NAME, sdCardDir, StorageType.SDCARD)?.let { list.add(it) }

        this.storages.clear()
        this.storages.addAll(list)
        view.updateStoragesList(this.storages.toArray(arrayOf<StorageDataModel>()))
    }

    override fun quickAccessItemClicked(file: QuickAccessFileDataModel) {
        val fileModel = makeFileModel(File(file.path))
        if (fileModel.type == FileType.FILE) {
            view.openActivity(getOpenFileIntent(fileModel))
        } else {
            // open the storage that contains the file
            getOpenFolderInStorageIntent(fileModel)?.let { view.openActivity(it) }
        }
    }

    override fun quickAccessItemLongClicked(file: QuickAccessFileDataModel) {
        val fileModel = makeFileModel(File(file.path))
        val date = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US).format(fileModel.date)

        val size = if (fileModel.type == FileType.FILE)
            fileModel.size
        else
            getSize(getFolderSizeBytes(File(fileModel.path)))

        view.showDetailsDialog(fileModel.name, size, date, fileModel.path)
    }

    override fun locateFile(path: String) {
        val parentFile = File(path).parentFile
        parentFile?.let { parent ->
            getOpenFolderInStorageIntent(makeFileModel(parent))?.let {
                view.openActivity(it)
            }
        }
    }

    override fun quickAccessEditModeStopped() {
        view.showEditQuickAccess()
    }

    override fun deleteQuickAccessFile(file: QuickAccessFileDataModel) {
        model.deleteQuickAccessFile(file, object : SimpleSuccessAndFailureCallback<Boolean> {
            override fun onSuccess(data: Boolean) {
                if (file.type == QuickAccessFileType.PINED)
                    loadPinedFiles()
                else
                    loadRecentFiles()
                Toast.makeText(view.context(), "file deleted", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(message: String) {
                Toast.makeText(view.context(), "can't deleted file", Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun loadPinedFiles() {
        model.getQuickAccessFiles(QuickAccessFileType.PINED,
            object : SimpleSuccessAndFailureCallback<List<QuickAccessFileDataModel>> {
                override fun onSuccess(data: List<QuickAccessFileDataModel>) {
                    updateQuickAccessCard(data)
                }

                override fun onFailure(message: String) {
                    updateQuickAccessCard(listOf())
                    Log.d(TAG, "onFailure: no pined files")
                }
            })
    }

    override fun loadRecentFiles() {
        model.getQuickAccessFiles(
            QuickAccessFileType.RECENT,
            object : SimpleSuccessAndFailureCallback<List<QuickAccessFileDataModel>> {
                override fun onSuccess(data: List<QuickAccessFileDataModel>) {
                    updateQuickAccessCard(data.filter { it.type == QuickAccessFileType.RECENT })
                }

                override fun onFailure(message: String) {
                    updateQuickAccessCard(listOf())
                    Log.d(TAG, message)
                }
            })
    }

    override fun updateQuickAccessCard(list: List<QuickAccessFileDataModel>) {
        if (list.isEmpty())
            view.quickAccessIsEmpty()
        else {
            view.updateQuickAccessFilesList(list)
        }
    }

    override fun updateQuickAccessCardHeight(cardHeight: Int) {
        view.updateQuickAccessWindowHeight(cardHeight)
    }


    /**
     * this will return an intent with the data required to open the containing storage of the file
     * and to navigate to the file immediately
     * @param file the file that will get navigate to
     * @return the storage intent with the info needed to navigate to the file,
     * or null if the storage or the file dose not exists
     */
    private fun getOpenFolderInStorageIntent(file: FileDataModel): Intent? {
        var intent: Intent? = null

        // looping throw the storages to find tha right storage
        for (storage in storages) {
            if (storage is StorageDataModel && file.path.startsWith(storage.path) && File(file.path).exists()) {

                // getting the intent that will open the correct storage
                val navigateToFolderIntent = getOenStorageIntent(storage)

                // adding the data that will make the storage activity immediately navigate to the file
                navigateToFolderIntent?.putExtra(LOCATE_FOLDER_PATH_EXTRA, file.path)

                // save the intent
                intent = navigateToFolderIntent
            }
        }
        return intent
    }

}
