package com.awab.fileexplorer.model.utils.transfer_utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.JobIntentService
import com.awab.fileexplorer.model.types.TransferAction
import com.awab.fileexplorer.model.utils.PASTE_LOCATION_PATH_EXTRA
import com.awab.fileexplorer.model.utils.TRANSFER_ACTION_EXTRA
import com.awab.fileexplorer.model.utils.TRANSFER_FILES_PATHS_EXTRA
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File
import java.io.FileOutputStream

class TransferToInternalStorageService : JobIntentService(), TransferService {

    companion object {

        fun startWork(context: Context, intent: Intent) {
            stopWork = false
            enqueueWork(context, TransferToInternalStorageService::class.java, 11, intent)
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

    override fun onProgressUpdate(intent: Intent) {
        sendBroadcast(intent.apply { action = StorageView.ACTION_PROGRESS_UPDATE })
    }

    override fun onFinish(intent: Intent) {
        sendBroadcast(intent.apply { action = StorageView.ACTION_FINISH_TRANSFER })
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun createFile(newFile: File, destFolder: File): File? {

        val createdFolder = File(destFolder.absolutePath + File.separator + newFile.name)
        // if the file already exists it will get skipped
        if (createdFolder.createNewFile()) {
            return createdFolder
        }
        return null
    }

    override fun createFolder(newFolder: File, destFolder: File): File? {
        val createdFolder = File(destFolder.absolutePath + File.separator + newFolder.name)
        // if the folder already exists it will get skipped
        if (createdFolder.mkdir()) {
            return createdFolder
        }
        return null
    }

    override fun delete(file: File) {
        try {
            file.deleteRecursively()
        } catch (e: Exception) {
        }
    }

    override fun getOutputStream(file: File): FileOutputStream {
        return FileOutputStream(file)
    }
}