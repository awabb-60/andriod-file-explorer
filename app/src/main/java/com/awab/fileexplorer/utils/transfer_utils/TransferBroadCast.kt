package com.awab.fileexplorer.utils.transfer_utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.awab.fileexplorer.utils.PASTE_LOCATION_STORAGE_MODEL_EXTRA
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.utils.data.types.StorageType

class TransferBroadCast : BroadcastReceiver() {

    companion object {
        const val ACTION = "com.awab.fileexplorer.ACTION_TRANSFER"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null)
            startTransferService(context, intent)
    }

    private fun startTransferService(context: Context, intent: Intent) {
        val pasteStorage = intent.getParcelableExtra<StorageDataModel>(PASTE_LOCATION_STORAGE_MODEL_EXTRA)
        when (pasteStorage?.storageType) {
            StorageType.INTERNAL -> TransferToInternalStorageService.startWork(context, intent)
            StorageType.SDCARD -> TransferToSDCardService.startWork(context, intent)
            else->{}
        }
    }
}
