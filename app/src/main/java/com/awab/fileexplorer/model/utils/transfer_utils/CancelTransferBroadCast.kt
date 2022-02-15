package com.awab.fileexplorer.model.utils.transfer_utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CancelTransferBroadCast : BroadcastReceiver() {

    companion object{
        const val ACTION = "com.awab.fileexplorer.ACTION_CANCEL"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        TransferToInternalStorageService.cancelWork()
        TransferToSDCardService.cancelWork()
    }
}
