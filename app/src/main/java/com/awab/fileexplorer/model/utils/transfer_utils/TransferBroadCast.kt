package com.awab.fileexplorer.model.utils.transfer_utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.awab.fileexplorer.model.data_models.StorageModel
import com.awab.fileexplorer.model.types.StorageType
import com.awab.fileexplorer.model.utils.PASTE_LOCATION_STORAGE_MODEL_EXTRA

class TransferBroadCast : BroadcastReceiver() {

    companion object {
        const val ACTION = "com.awab.fileexplorer.ACTION_TRANSFER"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null)
            startTransferService(context, intent)
    }

    private fun startTransferService(context: Context, intent: Intent) {
        val pasteStorage = intent.getParcelableExtra<StorageModel>(PASTE_LOCATION_STORAGE_MODEL_EXTRA)
        when (pasteStorage?.storageType) {
            StorageType.INTERNAL -> TransferToInternalStorageService.startWork(context, intent)
            StorageType.SDCARD -> TransferToSDCardService.startWork(context, intent)
            else->{}
        }
    }
}
