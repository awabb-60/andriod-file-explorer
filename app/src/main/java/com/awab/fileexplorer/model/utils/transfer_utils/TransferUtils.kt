package com.awab.fileexplorer.model.utils.transfer_utils

import com.awab.fileexplorer.model.utils.getInnerFilesCount
import com.awab.fileexplorer.model.utils.getInnerFoldersCount
import java.io.File

fun getTransferContains(selectedList: List<File>?): Int {
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

fun checkStorageSize(file: File, folder: File): Boolean {
    return file.length() < folder.freeSpace
}