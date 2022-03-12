package com.awab.fileexplorer.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val TAG = "permissionUtils"

fun allPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
    for (p in permissions) {
        if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED)
            return false
    }
    return true
}

fun requestPermissions(activity: Activity?, permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(activity!!, permissions, requestCode)
}

fun storageAccess(context: Context) {
    if (!allPermissionsGranted(context, INTERNAL_STORAGE_REQUIRED_PERMISSIONS))
        requestPermissions(context as Activity, INTERNAL_STORAGE_REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
    else
        Log.d(TAG, "storageAccess: all permissions are granted")
}