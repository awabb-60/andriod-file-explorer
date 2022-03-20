package com.awab.fileexplorer.utils.transfer_utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.JobIntentService
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.utils.*
import com.awab.fileexplorer.utils.data.types.TransferAction
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File
import java.io.FileOutputStream

class TransferToSDCardService : JobIntentService(), TransferService {

    /**
     * the uri that will get used to write to the sd card
     */
    private var treeUriFile: DocumentFile? = null

    companion object {

        fun startWork(context: Context, intent: Intent) {
            stopWork = false
            enqueueWork(context, TransferToSDCardService::class.java, 22, intent)
        }

        /**
         * used to stop the transfer work
         */
        var stopWork = false

        fun cancelWork() {
            stopWork = true
        }
    }


    override val stop: Boolean
        get() = stopWork


    override fun onHandleWork(intent: Intent) {
        // getting the tree uri
        val treeUri = intent.getParcelableExtra<Uri>(TREE_URI_FOR_TRANSFER_EXTRA)

        treeUri ?: return
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

            // start transferring
            if (transferAction != null) {
                TransferWorker(this, listFiles, pastFolder, transferAction).start()
            }
        }
    }

    override fun onDestroy() {
        treeUriFile = null
        super.onDestroy()
    }

    override fun onProgressUpdate(intent: Intent) {
        sendBroadcast(intent.apply { action = StorageView.ACTION_PROGRESS_UPDATE })
    }

    override fun onFinish(intent: Intent) {
        sendBroadcast(intent.apply { action = StorageView.ACTION_FINISH_TRANSFER })
    }

    override fun createFile(newFile: File, destFolder: File): File? {
        val parentFile = navigateToTreeFile(treeUriFile!!, destFolder.absolutePath)

        // if the file already exists it will get skipped
        if (parentFile?.findFile(newFile.name) != null)
            return null
        val createdFile = parentFile?.createFile(newFile.extension, newFile.name)

        return if (createdFile != null)
            File(destFolder.absolutePath + File.separator + createdFile.name)
        else
            null
    }

    override fun createFolder(newFolder: File, destFolder: File): File? {
        val parentFile = navigateToTreeFile(treeUriFile!!, destFolder.absolutePath)

        // if the folder already exists it will get skipped
        if (parentFile?.findFile(newFolder.name) != null)
            return null

        val createdFolder = parentFile?.createDirectory(newFolder.name)
        return if (createdFolder != null)
            File(destFolder.absolutePath + File.separator + createdFolder.name)
        else
            null
    }

    override fun delete(file: File) {
        val targetedFile = navigateToTreeFile(treeUriFile!!, file.absolutePath)
        targetedFile?.delete()
    }

    override fun getOutputStream(file: File): FileOutputStream {
        val documentFile = navigateToTreeFile(treeUriFile!!, file.absolutePath)!!
        return contentResolver.openOutputStream(documentFile.uri) as FileOutputStream
    }
}