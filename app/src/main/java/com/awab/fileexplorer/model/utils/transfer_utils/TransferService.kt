package com.awab.fileexplorer.model.utils.transfer_utils

import android.content.Intent
import java.io.File
import java.io.FileOutputStream

/**
 * the toole that the transfer worker will use to do operation on the storage
 */
interface TransferService {

    val stop: Boolean

    fun onProgressUpdate(intent: Intent)

    fun onFinish(intent: Intent)

    fun showToast(message: String)

    fun createFile(newFile: File, destFolder: File): File?

    fun createFolder(newFolder: File, destFolder: File): File?

    fun delete(file: File)

    fun getOutputStream(file: File): FileOutputStream
}