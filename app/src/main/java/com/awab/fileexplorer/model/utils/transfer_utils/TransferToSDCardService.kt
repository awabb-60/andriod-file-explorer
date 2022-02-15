package com.awab.fileexplorer.model.utils.transfer_utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.model.types.TransferAction
import com.awab.fileexplorer.model.utils.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class TransferToSDCardService : JobIntentService() {
    private val progressUpdateAfter = 2000

    private val onProgressLam: (max: Int, progress: Int) ->
    Unit = { max, p ->
        val progressIntent = Intent(PROGRESS_INTENT).apply {
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

    /**
     * the uri that will get used to write to the sd card
     */
    private var treeUriFile: DocumentFile? = null

    companion object {

        fun startWork(context: Context, intent: Intent) {
            stop = false
            enqueueWork(context, TransferToSDCardService::class.java, 22, intent)
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

        // getting the tree uri
        val treeUri = intent.getStringExtra(TREE_URI_FOR_TRANSFER_EXTRA)?.toUri()
        if (treeUri != null)
            treeUriFile = DocumentFile.fromTreeUri(this, treeUri)


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
                Log.d("TAG", "onHandleWork: COPY")
//                copy(listFiles, pastFolder)
            else if (transferAction == TransferAction.MOVE)
                Log.d("TAG", "onHandleWork: MOVE")
//                move(listFiles, pastFolder)
        }

        val finishCopyIntent = Intent(FINISH_COPY_INTENT)
        sendBroadcast(finishCopyIntent)
    }

    override fun onDestroy() {
        treeUriFile = null
        super.onDestroy()
    }

    private fun copy(files: List<File>, to: File) {
        files.forEach {
            if (stop)
                return
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

    private fun delete(file: File) {
        val parentFile = navigateToParentTreeFile(treeUriFile, file.absolutePath)
        val targetedFile = parentFile?.findFile(file.name)
        targetedFile?.delete()
    }

    private fun copyFileToFolder(file: File, folder: File): Boolean {
        if (!folder.isDirectory && checkStorageSize(file, folder)) {
            return false
        }
//    if the copying was successful return true
        return try {
            val newFile = createFile(file, folder) ?: return false

            val success = bufferCopyToSDCard(file, newFile, onProgressLam)
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

    private fun checkStorageSize(file: File, folder: File): Boolean {
        return file.length() < folder.freeSpace
    }

    private fun createFile(file: File, destFolder: File): File? {
        val parentFile = navigateToParentTreeFile(treeUriFile, destFolder.absolutePath)
        val newFile = parentFile?.findFile(destFolder.name)?.createFile(file.extension, file.name)

        return if (newFile != null)
            File(destFolder.absolutePath + File.separator + file.name)
        else
            null
    }

    private fun createFolder(folder: File, destFolder: File): File? {
        val parentFile = navigateToParentTreeFile(treeUriFile, destFolder.absolutePath)
        val newFolder = parentFile?.findFile(destFolder.name)?.createDirectory(folder.name)
        return if (newFolder != null)
            File(destFolder.absolutePath + File.separator + newFolder.name)
        else
            null
    }

    private fun navigateToParentTreeFile(treeDocumentFile: DocumentFile?, filePath: String): DocumentFile? {
//        removing the name from the path
        var innerPath = ""

//        removing the tree storage name  (sd card) and the file name
        val l = filePath.split(File.separator).dropWhile { it != treeDocumentFile?.name }.drop(1).dropLast(1)

        l.forEach { innerPath += it }

//        innerPath = innerPath.removeRange(0, filePath.indexOf(storageName) + storageName.length + 1)

        var file = treeDocumentFile
//        navigating to it parent
        for (fileName in innerPath.split(File.separator).filter { it != "" }) {
            file = file?.findFile(fileName)
            if (file == null)
                break
        }
        return file
    }

    private fun bufferCopyToSDCard(from: File, to: File, onProgress: (Int, Int) -> Unit): Boolean {
        var progress = 0

        inProgressFileName = from.name

        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        try {
            fis = FileInputStream(
                from.absolutePath
            )
            val parentFile = navigateToParentTreeFile(treeUriFile, to.absolutePath)
            val file = parentFile?.findFile(to.name)
            fos = contentResolver.openOutputStream(file?.uri!!) as FileOutputStream
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
                    return false
                }
            }
            return true
        } catch (e: Exception) {
            fis?.close()
            fos?.flush()
            fos?.close()
            return false
        } finally {
            fis?.close()
            fos?.close()
            onProgress.invoke(100, 100)
        }
    }
}


