package com.awab.fileexplorer.model.utils

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CopyServices : JobIntentService() {
    private val progressUpdateAfter = 2000

    private val onProgressLam: (max: Int, progress: Int, done: Int, left: Int, name: String) ->
    Unit = { max, p, done, left, name ->
        val progressIntent = Intent(PROGRESS_INTENT).apply {
            putExtra(MAX_PROGRESS_EXTRA, max)
            putExtra(PROGRESS_EXTRA, p)
            putExtra(DONE_AND_LEFT_EXTRA, "$done of $left")
            putExtra(CURRENT_COPY_ITEM_NAME_EXTRA, name)
        }
        sendBroadcast(progressIntent)
    }

    companion object {
        private var treeUriFile: DocumentFile? = null
        private var externalStoragePath: String? = null

        private var cancelCopy = false

        private var leftCount = 0
        private var doneCount = 0

        private var currentCopyItemName = ""

        fun startWork(context: Context, intent: Intent) {
            enqueueWork(context, CopyServices::class.java, 123, intent)
        }

        fun cancelCopy() {
            cancelCopy = true
        }
    }


    override fun onHandleWork(intent: Intent) {
        cancelCopy = false
        leftCount = 0
        doneCount = 0
        currentCopyItemName = ""

        val treeUri = intent.getStringExtra(TREE_URI_FOR_COPY_EXTRA)?.toUri()
        if (treeUri != null)
            treeUriFile = DocumentFile.fromTreeUri(this, treeUri)

        externalStoragePath = intent.getStringExtra(EXTERNAL_STORAGE_PATH_EXTRA)

        val pastLocation = intent.getStringExtra(PASTE_LOCATION)
        val list = intent.getStringArrayExtra(COPY_PATHS_EXTRA)
        if (list != null && pastLocation != null) {
            val pastFolder = File(pastLocation)
            val listFiles = list.map { File(it) }

            leftCount = getContains(listFiles)

            val copyType = intent.getStringExtra(COPY_TYPE_EXTRA)
            if (copyType == COPY)
                copy(listFiles, pastFolder)
            else if (copyType == MOVE)
                move(listFiles, pastFolder)
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
            if (cancelCopy)
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
                } catch (e: Exception) { }
            } else if (it.isDirectory)
                try {
//            copying the folder and what inside it
                    val success = copyFolderToFolder(it, to)
                    if (success) {
//                deleting the folder and what inside it
                        delete(it)
                    }
                } catch (e: Exception) { }
        }
    }

    private fun delete(file: File) {
        if (fileInSDCard(file)){
            val parentFile = navigateToParentTreeFile(treeUriFile, file.absolutePath)
            val targetedFile = parentFile?.findFile(file.name)
            targetedFile?.delete()
        }else{
            try {
                file.deleteRecursively()
            }catch (e:Exception){}
        }

    }

    private fun copyFileToFolder(file: File, folder: File): Boolean {
        if (!folder.isDirectory && checkStorageSize(file, folder)) {
            return false
        }
//    if the copying was successful return true
        return try {
            val newFile = createFile(file, folder) ?: return false

            val success = bufferCopy(file, newFile, onProgressLam)
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

    private fun getContains(selectedList: List<File>?): Int {
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

    private fun createFile(file: File, destFolder: File): File? {
//        sd card
        if (fileInSDCard(destFolder)) {
            val parentFile = navigateToParentTreeFile(treeUriFile, destFolder.absolutePath)
            val newFile = parentFile?.findFile(destFolder.name)?.createFile(file.extension, file.name)

            return if (newFile != null)
                File(destFolder.absolutePath + File.separator + file.name)
            else
                null

        } else {
            val newFile = File(destFolder.absolutePath + File.separator + file.name)
            if (newFile.createNewFile()) {
                return newFile
            }
            return null
        }
    }

    private fun createFolder(folder: File, destFolder: File): File? {
//        sd card
        if (fileInSDCard(destFolder)) {
            val parentFile = navigateToParentTreeFile(treeUriFile, destFolder.absolutePath)
            val newFolder = parentFile?.findFile(destFolder.name)?.createDirectory(folder.name)
            return if (newFolder != null)
                File(destFolder.absolutePath + File.separator + newFolder.name)
            else
                null
        } else {
            val newFolder = File(destFolder.absolutePath + File.separator + folder.name)
            if (newFolder.mkdir()) {
                return newFolder
            }
            return null
        }
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

    private fun fileInSDCard(folder: File): Boolean {
        externalStoragePath ?: return false
        return folder.absolutePath.startsWith(externalStoragePath!!)
    }

    private fun bufferCopy(from: File, to: File, onProgress: (Int, Int, Int, Int, String) -> Unit): Boolean {
        var progress = 0

        currentCopyItemName = from.name

        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        try {
            fis = FileInputStream(
                from.absolutePath
            )
            fos = if (fileInSDCard(to)) {
                val parentFile = navigateToParentTreeFile(treeUriFile, to.absolutePath)
                val file = parentFile?.findFile(to.name)
                contentResolver.openOutputStream(file?.uri!!) as FileOutputStream
            } else {
                FileOutputStream(
                    to.absolutePath
                )
            }
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
                        doneCount,
                        leftCount,
                        currentCopyItemName
                    )
                    progress = 0
                }
                if (cancelCopy) {

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
            onProgress.invoke(100, 100, doneCount, leftCount, currentCopyItemName)
        }
    }
}


