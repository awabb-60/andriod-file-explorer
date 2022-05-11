package com.awab.fileexplorer.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val TAG = "permissionUtils"

fun allPermissionsGranted(context: Context): Boolean {
    if (isAPI30AndAbove())
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED

    // for lower versions
    for (p in INTERNAL_STORAGE_REQUIRED_PERMISSIONS) {
        if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED)
            return false
    }
    return true
}

fun requestPermissions(activity: Activity?) {
    if (isAPI30AndAbove()) {
        ActivityCompat.requestPermissions(
            activity!!, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
        return
    }

    // for lower versions
    ActivityCompat.requestPermissions(activity!!, INTERNAL_STORAGE_REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
}

fun storageAccess(context: Context) {
    if (!allPermissionsGranted(context))
        requestPermissions(context as Activity)
    else
        Log.d(TAG, "storageAccess: all permissions are granted")
}


fun isAPI30AndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

fun isBetweenAPI23And30() = Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.Q