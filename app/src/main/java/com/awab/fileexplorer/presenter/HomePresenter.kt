package com.awab.fileexplorer.presenter

import android.content.Intent
import android.provider.MediaStore
import com.awab.fileexplorer.R
import com.awab.fileexplorer.model.data_models.StorageModel
import com.awab.fileexplorer.model.types.StorageType
import com.awab.fileexplorer.presenter.contract.HomePresenterContract
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.view.MediaActivity
import com.awab.fileexplorer.view.contract.HomeView
import com.awab.fileexplorer.view.StorageActivity
import java.io.File
import java.lang.Exception


class HomePresenter(private val homeView: HomeView): HomePresenterContract {

    private  val TAG = "HomePresenter"

    override val view: HomeView
        get() = homeView

    override fun openStorage(it: StorageModel) {
        if (!allPermissionsGranted(view.context(), INTERNAL_STORAGE_REQUIRED_PERMISSIONS)){
            view.checkForPermissions()
            return
        }
        val storageIntent = Intent(view.context(), StorageActivity::class.java).apply {
            putExtra(STORAGE_PATH_EXTRA, it.path)
            putExtra(STORAGE_DISPLAY_NAME_EXTRA, it.name)
            // the type that will determine the presenter
            putExtra(STORAGE_TYPE_EXTRA, it.storageType)
        }
        view.openActivity(storageIntent)
    }

    override fun mediaItemClicked(id: Int) {
        val mediaIntent = Intent(view.context(), MediaActivity::class.java)
        when(id){
            R.id.btnMediaImages->{
                val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                mediaIntent.putExtra(MEDIA_CONTENT_URI  ,uri)
                view.openActivity(mediaIntent)
            }
            R.id.btnMediaVideo->{
                val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                mediaIntent.putExtra(MEDIA_CONTENT_URI  ,uri)
                view.openActivity(mediaIntent)
            }
            R.id.btnMediaAudio->{
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                mediaIntent.putExtra(MEDIA_CONTENT_URI  ,uri)
                view.openActivity(mediaIntent)
            }
            R.id.btnMediaDocs->{
                mediaIntent.putExtra(MEDIA_CONTENT_DOCS  ,true)
                view.openActivity(mediaIntent)
            }
        }
    }

    fun makeStoragesModels(storages: List<File?>): List<StorageModel> {
        val list = mutableListOf<StorageModel>()
        val internalDir = storages[0]
        if (internalDir != null)
            makeStorageModel(INTERNAL_STORAGE_DISPLAY_NAME, internalDir, StorageType.INTERNAL)?.let { list.add(it) }

//        making the sd card modes
        if (storages.size == 1 )
            return list

        val sdCardDir = storages[1]
        if (sdCardDir != null)
            makeStorageModel(EXTERNAL_SDCARD_DISPLAY_NAME, sdCardDir, StorageType.SDCARD)?.let { list.add(it) }
        return list
    }

    private fun makeStorageModel(name: String, file:File, type: StorageType): StorageModel? {
        return try {
            val freeSize = getSize(file.freeSpace)
            val totalSize = getSize(file.totalSpace)
            val displaySize = "$freeSize free of $totalSize"
             StorageModel(name = name,
                size = displaySize,
                freeSizeBytes = file.freeSpace,
                totalSizeBytes = file.totalSpace,
                path = file.absolutePath,
                storageType = type
            )
        }catch (e: Exception){null}
    }
}