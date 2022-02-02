package com.awab.fileexplorer.model.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.awab.fileexplorer.model.data_models.BreadcrumbsModel
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.types.FileType
import com.awab.fileexplorer.model.types.MimeType
import java.io.File
import java.util.*

private const val TAG = "fileUtils"

fun isEmpty(file: File): Boolean {
    if (file.isFile)
        return true
    return try {
        val files = file.listFiles()
        files!!.isEmpty()
    } catch (e: Exception) {
        true
    }
}

fun makeFilesList(
    file: File,
    showHidingFiles: Boolean = false,
    sortingType: String = SORTING_TYPE_NAME,
    sortingOrder: String = SORTING_ORDER_DEC
): List<FileModel> {

    return try {
//         getting and sorting the list
        val listFiles = when (sortingType) {
            SORTING_TYPE_SIZE -> {
                if (sortingOrder == SORTING_ORDER_ASC)
                    file.listFiles()!!.sortedBy { it.length() }
                else
                    file.listFiles()!!.sortedByDescending { it.length() }
            }
            SORTING_TYPE_DATE -> {
                if (sortingOrder == SORTING_ORDER_ASC)
                    file.listFiles()!!.sortedBy { it.lastModified() }
                else
                    file.listFiles()!!.sortedByDescending { it.lastModified() }
            }
//            sorting by name
            else -> {
                if (sortingOrder == SORTING_ORDER_ASC)
                    file.listFiles()!!.sortedBy { it.name }
                else
                    file.listFiles()!!.sortedByDescending { it.name }
            }
        }
//        turn it into file models
        var modelsList = makeFileModels(listFiles)
//        filtering the hiding files
        modelsList = modelsList.filter { !it.name.startsWith('.') || showHidingFiles }
//        shoe the folder first
        modelsList.sortedBy { it.type == FileType.FILE }
    } catch (e: Exception) {
        Log.d(TAG, "Error reading the content of this file:${file.name}")
        listOf()
    }
}


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

fun getInnerFilesCount(file: File): Int {
    return file.walkTopDown().filter { it.isFile }.count()
}

fun getInnerFoldersCount(file: File): Int {
    return file.walkTopDown().drop(1).filter { it.isDirectory }.count()
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

fun getMime(type: String): MimeType {
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

fun makeFileModels(listFiles: List<File>): List<FileModel> =
    listFiles.map {
        FileModel(
            name = it.name,
            path = it.absolutePath,
            size = getSize(it.length()),
            date = Date(it.lastModified()),
            type = getFileType(it),
            mimeType = getMime(it),
            uri = it.toUri(),
            isEmpty = isEmpty(it)
        )
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

fun deleteFolderIO(path: String) {
    try {
        val file = File(path)
        if (file.deleteRecursively())
            Log.d(TAG, "${file.name} was deleted successfully")
        else
            Log.d(TAG, "error deleting this folder")
    } catch (e: Exception) {
        Log.d(TAG, "error deleting this file")
    }
}

fun deleteFileIO(path: String) {
    try {
        val file = File(path)
        if (file.delete())
            Log.d(TAG, "file ${file.name} deleted successfully")
        else
            Log.d(TAG, "file ${file.name} not deleted successfully")
    } catch (e: Exception) {
        Log.d(TAG, "error deleting this file")
    }
}

fun folderAlreadyExist(file: File): Boolean {
    return try {
        val list = file.parentFile!!.listFiles()!!
        val mli = list.map { listOf(it.name, it.isDirectory) }
        mli.contains(listOf(file.name, true))
    } catch (e: Exception) {
        true
    }
}

fun nameAlreadyExist(file: File): Boolean {
    return try {
        val list = file.parentFile!!.listFiles()!!
        val mli = list.map { it.name }
        mli.contains(file.name)
    } catch (e: Exception) {
        true
    }
}

fun getInnerFiles(file: File, showHidingFiles: Boolean): List<File> {
    val list = file.listFiles()
    list ?: return listOf()

    val innerList = mutableListOf<File>()
    for (currentFile in list) {
//      skipping the hiding files and what inside it the hiding folders
        if (currentFile.isHidden && !showHidingFiles)
            continue

//      adding the file or folder
        innerList.add(currentFile)

//      adding what inside the folder
        if (currentFile.isDirectory) {
            getInnerFiles(currentFile, showHidingFiles).forEach {
                innerList.add(it)
            }
        }
    }
    return innerList
}

fun getInnerFiles2(file: File, showHidingFiles: Boolean): List<File> {
    var innerList = file.walkTopDown().toList()
    if (!showHidingFiles)
        innerList = innerList.filter { !it.isHidden }
//    dropping the first file which is the parent file
    return innerList.toList().drop(1)
}

/**
 * get search result from folder
 */
fun getSearchResults(folder: File, text: String, showHidingFiles: Boolean = false): List<FileModel> {
    val innerFiles = getInnerFiles(folder, showHidingFiles).filter { text.lowercase() in it.name.lowercase() }

//    returning the files models and showing the folders first
    return makeFileModels(innerFiles).sortedBy { it.type == FileType.FILE && it.isEmpty }
}

/**
 * get search result from a list of files
 */
fun getSearchResults(filesList: List<FileModel>, text: String): List<FileModel> {
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
): MutableList<BreadcrumbsModel> {
    val list = mutableListOf<BreadcrumbsModel>()

//    adding the storage breadcrumb item
    list.add(BreadcrumbsModel(storageName, storagePath))

//    if the top path only have the storage folder
    if (topPath == storagePath)
        return list

    var itemPath = storagePath
    topPath.removePrefix(storagePath).forEach {
        if (it == File.separatorChar && itemPath.isNotEmpty()){
            val item = BreadcrumbsModel(File(itemPath).name, itemPath)
            list.add(item)
        }
        itemPath += it
    }
    list.add(BreadcrumbsModel(File(itemPath).name, itemPath))

    return list
}