package com.awab.fileexplorer.model.utils.transfer_utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.JobIntentService
import com.awab.fileexplorer.model.data_models.TransferInfo
import com.awab.fileexplorer.model.types.TransferAction
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class TransferToInternalStorageService : JobIntentService() {

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
            if (transferAction != null) {
                transfer(listFiles,pastFolder, transferAction)
            }else // intent with no data means nothing happened
                sendBroadcast(Intent(FINISH_COPY_INTENT))
        }
    }

    private fun transfer(files: List<File>, to: File, action: TransferAction) {
        var allSuccessful = true
        for (it in files) {
            if (stop) {
                Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show()
                allSuccessful = false
                break
            }
            if (it.isFile) {
                    val success = copyFileToFolder(it, to)
                    if (!success) {
                        allSuccessful = success
                    }
            } else if (it.isDirectory){
//            copying the folder and what inside it
                    val success = copyFolderToFolder(it, to)
                    if (!success) { // deleting the folder and what inside it
                        allSuccessful = success
                    }
                }
            }
        // the move was successful... finish the Transfer
        // if allSuccessful was true the intent will tell the main presenter to delete the moved files
        val info = TransferInfo(allSuccessful,action)
        val finishIntent = Intent(FINISH_COPY_INTENT).putExtra(TRANSFER_INFO_EXTRA, info)
        sendBroadcast(finishIntent)
    }

    private fun copyFileToFolder(file: File, folder: File): Boolean {
        if (!folder.isDirectory && checkStorageSize(file, folder)) {
            return false
        }
//    if the copying was successful return true
        return try {
            val newFile = createFile(file, folder)
            if (newFile == null){
                Toast.makeText(this, "skipped ${file.name}", Toast.LENGTH_SHORT).show()
                return false
            }
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
            val newFolder = createFolder(srcFolder, desFolder)
            if (newFolder == null){
                Toast.makeText(this, "skipped ${srcFolder.name}", Toast.LENGTH_SHORT).show()
                return false
            }
            doneCount++

            // show that the files is transferred successfully
            onProgressLam.invoke(100, 100)

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

    private fun createFile(file: File, destFolder: File): File? {

        val newFile = File(destFolder.absolutePath + File.separator + file.name)
        // if the file already exists it will get skipped
        if (newFile.createNewFile()) {
            return newFile
        }
        return null
    }

    private fun createFolder(folder: File, destFolder: File): File? {
        val newFolder = File(destFolder.absolutePath + File.separator + folder.name)
        // if the folder already exists it will get skipped
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
        } catch (e: Exception) { }
    }

    private fun bufferCopyToInternalStorage(from: File, to: File, onProgress: (Int, Int) -> Unit): Boolean {
        var progress = 0

        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        try {
            // making input and the output stream
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