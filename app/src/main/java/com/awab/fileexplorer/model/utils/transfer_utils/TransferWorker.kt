package com.awab.fileexplorer.model.utils.transfer_utils

import android.content.Intent
import com.awab.fileexplorer.model.data_models.TransferInfo
import com.awab.fileexplorer.model.types.TransferAction
import com.awab.fileexplorer.model.utils.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class TransferWorker(
    private val service: TransferService,
    private val listFiles: List<File>,
    private val pastFolder: File,
    private val transferAction: TransferAction
) {

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

    private val stop: Boolean
        get() = service.stop

    private val messages = ArrayList<String>()

    fun start() {
        // count all the files and folders in the list
        totalCount = getTransferContains(listFiles)
        doneCount = 0

        // start transferring
        transfer(listFiles, pastFolder, transferAction)
    }


    private fun transfer(files: List<File>, to: File, action: TransferAction) {
        var allSuccessful = true
        for (it in files) {
            if (stop) {
                messages.add("Canceled")
                allSuccessful = false
                break
            }
            if (it.isFile) {
                val success = copyFileToFolder(it, to)
                if (!success) {
                    allSuccessful = success
                }
            } else if (it.isDirectory) {
//            copying the folder and what inside it
                val success = copyFolderToFolder(it, to)
                if (!success) {
                    allSuccessful = success
                }
            }
        }
        // the move was successful... finish the Transfer
        // if allSuccessful was true the intent will tell the main presenter to delete the moved files
        val info = TransferInfo(allSuccessful, messages, action)
        val finishIntent = Intent().putExtra(TRANSFER_INFO_EXTRA, info)
        service.onFinish(finishIntent)
    }

    private fun copyFileToFolder(file: File, folder: File): Boolean {
        if (!folder.isDirectory && checkStorageSize(file, folder)) {
            return false
        }
//    if the copying was successful return true
        return try {
            val newFile = service.createFile(file, folder)
            if (newFile == null) {
                messages.add("skipped ${file.name}")
                return false
            }
            val success = bufferCopy(file, newFile)
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
            val newFolder = service.createFolder(srcFolder, desFolder)
            if (newFolder == null) {
                messages.add("skipped ${srcFolder.name}")
                return false
            }
            doneCount++

            // show that the files is transferred successfully
            progress(100, 100)

            //    copying the inner files
            srcFolder.listFiles()?.forEach {
//            copying the files
                if (it.isFile)
                    copyFileToFolder(it, newFolder)
//            copying the folder and what inside it
                else
                    copyFolderToFolder(it, newFolder)
            }
//        coping was successful
            return true
        } catch (E: Exception) {
            return false
        }
    }

    private fun progress(progress: Int, max: Int) {
        val progressIntent = Intent().apply {
            putExtra(MAX_PROGRESS_EXTRA, max)
            putExtra(PROGRESS_EXTRA, progress)
            putExtra(DONE_AND_LEFT_EXTRA, "$doneCount of $totalCount")
            putExtra(CURRENT_COPY_ITEM_NAME_EXTRA, inProgressFileName)
        }
        service.onProgressUpdate(progressIntent)
    }

    private fun getTransferContains(selectedList: List<File>?): Int {
        var count = 0
        selectedList?.forEach {
            if (it.isFile)
                count++
            else {
                count++
                count += getInnerFilesCount(File(it.path), true)
                count += getInnerFoldersCount(File(it.path), true)
            }
        }
        return count
    }

    private fun checkStorageSize(file: File, folder: File): Boolean {
        return file.length() < folder.freeSpace
    }

    private fun bufferCopy(from: File, to: File): Boolean {
        var progress = 0

        inProgressFileName = from.name

        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null

        try {
            fis = FileInputStream(from.absolutePath)
            fos = service.getOutputStream(to)

            var c: Int
            val totalSize = fis.channel.size().toInt()
            val b = ByteArray(1024)

            // to show the start of the progress
            progress(
                fos.channel.size().toInt(),
                totalSize
            )
            while (fis.read(b).also { c = it } != -1) {
                fos.write(b, 0, c)
                progress++
                if (progress == progressUpdateAfter) {
                    progress(
                        fos.channel.size().toInt(),
                        totalSize
                    )
                    progress = 0
                }
                if (stop) {
                    // delete the unfinished file
                    service.delete(to)
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
            progress(100, 100)
        }
    }
}