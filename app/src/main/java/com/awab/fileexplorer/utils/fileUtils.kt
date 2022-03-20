package com.awab.fileexplorer.utils

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.utils.data.data_models.BreadcrumbsDataModel
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.types.FileType
import com.awab.fileexplorer.utils.data.types.MimeType
import java.io.File
import java.util.*

private const val TAG = "fileUtils"

fun isEmpty(file: File, showHidingFiles: Boolean): Boolean {
    if (file.isFile)
        return true
    return try {
        val files = file.listFiles()!!
        if (showHidingFiles)
            files.isEmpty()
        else // the hidden files will not get counted
            files.none { !it.isHidden }
    } catch (e: Exception) {
        true
    }
}

fun makeFilesList(
    file: File,
    sortingBy: String,
    sortingOrder: String,
    showHidingFiles: Boolean
): List<FileDataModel> {

    return try {
//         getting and sorting the list
        val listFiles = when (sortingBy) {
            SORTING_BY_NAME -> {
                if (sortingOrder == SORTING_ORDER_ASC)
                    file.listFiles()!!.sortedBy { it.name }
                else
                    file.listFiles()!!.sortedByDescending { it.name }
            }
            SORTING_BY_SIZE -> {
                if (sortingOrder == SORTING_ORDER_ASC)
                    file.listFiles()!!.sortedBy { it.length() }
                else
                    file.listFiles()!!.sortedByDescending { it.length() }
            }
            SORTING_BY_DATE -> {
                if (sortingOrder == SORTING_ORDER_ASC)
                    file.listFiles()!!.sortedBy { it.lastModified() }
                else
                    file.listFiles()!!.sortedByDescending { it.lastModified() }
            }
            else -> { // default sorting by name
                    file.listFiles()!!.sortedBy { it.name }
            }
        }
//        turn it into file models
        var modelsList = makeFileModels(listFiles, showHidingFiles)

//        filtering the hiding files
        modelsList = modelsList.filter { !it.name.startsWith('.') || showHidingFiles }

//        shoe the folder first
        modelsList.sortedBy { it.type == FileType.FILE }
    } catch (e: Exception) {
        Log.d(TAG, "Error reading the content of this file:${file.name}")
        listOf()
    }
}

fun makeFileModels(listFiles: List<File>, showHiddenFiles: Boolean = false): List<FileDataModel> =
    listFiles.map {
        makeFileModel(it, showHiddenFiles)
    }

fun makeFileModel(file: File, showHiddenFiles: Boolean = false) = FileDataModel(
    name = file.name,
    path = file.absolutePath,
    size = getSize(file.length()),
    date = Date(file.lastModified()),
    type = getFileType(file),
    mimeType = getMime(file),
    uri = file.toUri(),
    isEmpty = isEmpty(file, showHiddenFiles)
)

fun getSize(sizeInBytes: Long): String {
    var size = sizeInBytes.div(1024.0)
    var ex = "KB"
    if (size > 1000) {
        size = size.div(1024.0)
        ex = "MB"
    }
    if (size > 900) {
        size = size.div(1024.0)
        ex = "GB"
    }
    return String.format("%.2f", size) + ex
}

fun getFolderSizeBytes(folder: File): Long {
    var sizeBytes = 0L
    for (it in folder.walkTopDown())
        if (it.isFile) sizeBytes += it.length()
    return sizeBytes
}

/**
 * calculate the total size of the files and folders in the list
 * @param list the list to find it size
 */
fun getTotalSize(list: List<FileDataModel>): String {
    var totalSizeBytes = 0L
    list.forEach {
        totalSizeBytes += if (it.type == FileType.FILE) {
            File(it.path).length()
        } else
            getFolderSizeBytes(File(it.path))
    }
    return getSize(totalSizeBytes)
}

/**
 * return a string that will show the number of files and folders (and what the folders contains) from the list
 * @param list the list of files to get it contains
 * @param countHiddenFiles true to cont the hidden files false otherwise
 */
fun getContains(list: List<FileDataModel>, countHiddenFiles: Boolean): String {
    var fileCount = 0
    var folderCount = 0
    list.forEach {
        if (it.type == FileType.FILE)
            fileCount++
        else {
            folderCount++
            fileCount += getInnerFilesCount(File(it.path), countHiddenFiles)
            folderCount += getInnerFoldersCount(File(it.path), countHiddenFiles)
        }
    }
    return "$fileCount Files, $folderCount Folders"
}

fun getInnerFilesCount(file: File, countHiddenFiles: Boolean): Int {
    return if (countHiddenFiles)
        file.walkTopDown().filter { it.isFile }.count()
    else // not counting the hidden files
        file.walkTopDown().filter { it.isFile }.filter { !it.isHidden }.count()
}

fun getInnerFoldersCount(file: File, countHiddenFiles: Boolean): Int {
    // drop(1) to drop the parent folder
    return if (countHiddenFiles)
        file.walkTopDown().drop(1).filter { it.isDirectory }.count()
    else
        file.walkTopDown().drop(1).filter { it.isDirectory }.filter { !it.isHidden }.count()
}

fun getFileType(file: File): FileType = if (file.isFile) FileType.FILE else FileType.DIRECTORY

fun getMime(file: File): MimeType {
    val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension) ?: return MimeType.UNKNOWN
    return when (type.split('/')[0]) {
        "audio" -> MimeType.AUDIO
        "image" -> MimeType.IMAGE
        "video" -> MimeType.VIDEO
        "text" -> {
            when {
                type.endsWith("html") -> MimeType.HTML
                type.endsWith("xml") -> MimeType.HTML
                type.endsWith("plain") -> MimeType.TEXT
                else -> MimeType.UNKNOWN
            }
        }
        "application" -> {
            when {
                type.endsWith("pdf") -> MimeType.PDF
                type.endsWith("ogg") -> MimeType.AUDIO
                type.endsWith("vnd.android.package-archive") -> MimeType.APPLICATION
                else -> MimeType.UNKNOWN
            }
        }
        else -> MimeType.UNKNOWN
    }
}

/**
 * when the new folder name is duplicated the function give a unique name
 */
fun getValidFolderName(file: File): File {
    var nameCounter = 1
    var newFolderPath = file.absolutePath + " ( $nameCounter )"

    while (File(newFolderPath).exists()) {
        nameCounter++
        newFolderPath = newFolderPath.dropLastWhile { it != '/' } + "${file.name} ( $nameCounter )"
    }
    return File(newFolderPath)
}

fun renameFileIO(path: String, newName: String): Boolean {
    val oldFile = File(path)
    val newFile = File(oldFile.path.removeSuffix(oldFile.name) + newName)

    if (newFile.exists()) {
        Log.d(TAG, "this name is already taken!")
        return false
    }

    return try {
        if (oldFile.renameTo(newFile)) {
            Log.d(TAG, "renaming was successful")
            true
        } else {
            Log.d(TAG, "renaming was not successful")
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun createFolderIO(file: File): Boolean {
    return try {
        var folder = file
        if (folder.exists()) {
            folder = getValidFolderName(folder)
        }
        if (folder.mkdir()) {
            Log.d(TAG, "Folder ${file.name} successfully created")
            true
        } else {
            Log.d(TAG, "unable to create this folder!")
            false
        }
    } catch (e: Exception) {
        Log.d(TAG, "unable to create this folder!")
        false
    }
}

/**
 * get search result from a list of files
 */
fun getSearchResults(filesList: List<FileDataModel>, text: String): List<FileDataModel> {
    val files = filesList.filter { text.lowercase() in it.name.lowercase() }

//    returning the files models and showing the folders first
    return files.sortedBy { it.type == FileType.FILE && it.isEmpty }
}

fun getApkIcon(context: Context, filePath: String): Drawable? {
    val pm = context.packageManager
    val pi = pm.getPackageArchiveInfo(filePath, 0)

    pi?.applicationInfo?.sourceDir = filePath
    pi?.applicationInfo?.publicSourceDir = filePath

    return pi?.applicationInfo?.loadIcon(pm)
}

fun recreateBreadcrumbsFromPath(
    storageName: String,
    storagePath: String,
    topPath: String
): MutableList<BreadcrumbsDataModel> {
    val list = mutableListOf<BreadcrumbsDataModel>()

//    adding the storage breadcrumb item
    list.add(BreadcrumbsDataModel(storageName, storagePath))

//    if the top path only have the storage folder
    if (topPath == storagePath)
        return list

    var itemPath = storagePath
    topPath.removePrefix(storagePath).forEach {
        if (it == File.separatorChar && itemPath.isNotEmpty()) {
            val item = BreadcrumbsDataModel(File(itemPath).name, itemPath)
            list.add(item)
        }
        itemPath += it
    }
    list.add(BreadcrumbsDataModel(File(itemPath).name, itemPath))
    return list
}

/**
 * this will navigate to  the Document file that has the filePath inside the given treeDocumentFile
 * and return it or null if the file doesn't exists
 * @param treeDocumentFile the parent file that contains the targeted file with the filePath
 * @param filePath tha path of the file inside the parent folder
 * @return the targeted file with filePath as a DocumentFile or null if the file doesn't exists
 * or the parent file doesn't contain it
 */
fun navigateToTreeFile(treeDocumentFile: DocumentFile, filePath: String): DocumentFile? {
    val storageName = treeDocumentFile.name ?: return null

    // the file path without the sd card storage path at the start
    val innerPath = filePath.removeRange(0, filePath.indexOf(storageName) + storageName.length)

    // the sd card storage tree document file
    var file: DocumentFile? = treeDocumentFile

    // navigating to it parent
    for (fileName in innerPath.split(File.separatorChar).filter { it != "" }) {
        file = file?.findFile(fileName)
        if (file == null)
            break
    }
    return file
}

/**
 * it will return an intent to open or to view a media file:Image, video ...
 * @param file the that will be open or viewed
 * @return an intent with ACTION_VIEW and has the data and type of the given file
 */
fun getOpenFileIntent(file: FileDataModel): Intent {
    if (file.mimeType == MimeType.APPLICATION) {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, file.mimeType.mimeString)
        }
    }
    return Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(file.uri, file.mimeType.mimeString)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}