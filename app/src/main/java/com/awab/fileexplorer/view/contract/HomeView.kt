package com.awab.fileexplorer.view.contract

import android.content.Context
import android.content.Intent

interface HomeView {

    /**
     * open an new activity with the given intent
     */
    fun openActivity(intent: Intent)

    /**
     * return the context of this view
     */
    fun context():Context

    /**
     * asks the user for permissions to use the app
     */
    fun checkForPermissions()
}