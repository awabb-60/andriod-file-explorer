package com.awab.fileexplorer.presenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.awab.fileexplorer.model.utils.CopyServices

class CopyBreadCast:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null && intent != null)
            CopyServices.startWork(context, intent)
    }
}
