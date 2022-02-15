package com.awab.fileexplorer.model.utils.transfer_utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.JobIntentService
import com.awab.fileexplorer.model.types.TransferAction
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class TransferToInternalStorageService : JobIntentService() {
    // the gap between progress update
    private val progressUpdateAfter = 2000

    // the function that will send updates to the view
    private val onProgressLam: (max: Int, progress: Int) ->
    Unit = { max, p ->
        val progressIntent = Intent(StorageView.ACTION_PROGRESS_UPDATE).apply {
            putExtra(MAX_PROGRESS_EXTRA, max)
            putExtra(PROGRESS_EXTRA, p)
            putExtra(DONE_AND_LEFT_EXTRA, "$doneCount of $totalCount")
            putExtra(CURRENT_COPY_ITEM_NAME_EXTRA, inProgressFileName)
        }
        sendBroadcast(progressIntent)
    }

    /**
     * the total number of files
     */
    private var totalCount = 0

    /**
     * the number of files that has been transferred
     */
    private var doneCount = 0

    /**
     * the current file getting transferred
     */
    private var inProgressFileName = ""

    companion object {

        fun startWork(context: Context, intent: Intent) {
            stop = false
            enqueueWork(context, TransferToInternalStorageService::class.java, 11, intent)
        }

        /**
         * used to stop the transfer work
         */
        var stop = false

        fun cancelWork() {
            stop = true
        }
    }

    override fun onHandleWork(intent: Intent) {
        // the paste location
        val pastLocation = intent.getStringExtra(PASTE_LOCATION_PATH_EXTRA)

        // the selected files
        val list = intent.getStringArrayExtra(TRANSFER_FILES_PATHS_EXTRA)

        // the transfer action
        val transferAction = intent.getParcelableExtra<TransferAction>(TRANSFER_ACTION_EXTRA)

        if (list != null && pastLocation != null) {
            val pastFolder = File(pastLocation)

            // turning the paths into files
            val listFiles = list.map { File(it) }

            // count all the files and folders in the list
            totalCount = getTransferContains(listFiles)
            doneCount = 0

            // start transferring
            if (transferAction == TransferAction.COPY)
                copy(listFiles, pastFolder)
            else if (transferAction == TransferAction.MOVE)
                move(listFiles, pastFolder)
        }

        // if transfer has not been canceled by the user
        if (!stop) {
            val finishCopyIntent = Intent(FINISH_COPY_INTENT)
            sendBroadcast(finishCopyIntent)
        }
    }

    private fun copy(files: List<File>, to: File) {
        files.forEach {
            if (stop){
                Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show()
                return
            }
            if (it.isFile)
                copyFileToFolder(File(it.path), to)
            else if (it.isDirectory)
                copyFolderToFolder(File(it.path), to)
        }
    }

    private fun move(files: List<File>, to: File) {
        files.forEach {
            if (it.isFile) {
                try {
                    val success = copyFileToFolder(it, to)
                    if (success) {
                        delete(it)
                    }
                } catch (e: Exception) {
                }
            } else if (it.isDirectory)
                try {
//            copying the folder and what inside it
                    val success = copyFolderToFolder(it, to)
                    if (success) {
//                deleting the folder and what inside it
                        delete(it)
                    }
                } catch (e: Exception) {
                }
        }
    }

    private fun copyFileToFolder(file: File, folder: File): Boolean {
        if (!folder.isDirectory && checkStorageSize(file, folder)) {
            return false
        }
//    if the copying was successful return true
        return try {
            val newFile = createFile(file, folder) ?: return false

            inProgressFileName = file.name
            val success = bufferCopyToInternalStorage(file, newFile, onProgressLam)
            doneCount++
            success
        } catch (e: Exception) {
            false
        }
    }

    private fun copyFolderToFolder(srcFolder: File, desFolder: File): Boolean {
        if (!srcFolder.isDirectory || !desFolder.isDirectory) {
            return false
        }
        try {
//        creating the new dir in the dest folder
            val newFolder = createFolder(srcFolder, desFolder) ?: return false
            doneCount++

            // copying the inner files
            srcFolder.listFiles()?.forEach {
                // copying the files
                if (it.isFile)
                    copyFileToFolder(it, newFolder)
                // copying the folder and what inside it
                else
                    copyFolderToFolder(it, newFolder)
            }
//        coping was successful
            return true
        } catch (E: Exception) {
            return false
        }
    }

    private fun checkStorageSize(file: File, folder: File): Boolean {
        return file.length() < folder.freeSpace
    }

    private fun createFile(file: File, destFolder: File): File? {

        val newFile = File(destFolder.absolutePath + File.separator + file.name)
        if (newFile.createNewFile()) {
            return newFile
        }
        return null
    }

    private fun createFolder(folder: File, destFolder: File): File? {
        val newFolder = File(destFolder.absolutePath + File.separator + folder.name)
        if (newFolder.mkdir()) {
            return newFolder
        }
        return null
    }

    /**
     * delete files from internal storage
     */
    private fun delete(file: File) {
        try {
            file.deleteRecursively()
        } catch (e: Exception) {
        }
    }

    private fun bufferCopyToInternalStorage(from: File, to: File, onProgress: (Int, Int) -> Unit): Boolean {
        var progress = 0

        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        try {
            fis = FileInputStream(
                from.absolutePath
            )
            fos = FileOutputStream(
                to.absolutePath
            )

            var c: Int
            val totalSize = fis.channel.size().toInt()
            val b = ByteArray(1024)

            while (fis.read(b).also { c = it } != -1) {
                fos.write(b, 0, c)
                progress++
                if (progress == progressUpdateAfter) {
                    onProgress.invoke(
                        totalSize,
                        fos.channel.size().toInt(),
                    )
                    progress = 0
                }
                if (stop) {
                    // delete the unfinished file
                    delete(to)
                    error("Canceled")
                }
            }
            return true
        } catch (e: Exception) {
            fos?.flush()
            return false
        } finally {
            fis?.close()
            fos?.close()
            onProgress.invoke(
                100, 100
            )
        }
    }
}