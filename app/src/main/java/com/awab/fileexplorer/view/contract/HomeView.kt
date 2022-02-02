package com.awab.fileexplorer.view.contract

import android.content.Context
import android.content.Intent

interface HomeView {

    fun openActivity(intent: Intent)

    fun context():Context

    fun checkForPermissions()
}