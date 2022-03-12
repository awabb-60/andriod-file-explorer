package com.awab.fileexplorer.model

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.awab.fileexplorer.model.contrancts.StorageModel
import com.awab.fileexplorer.utils.*

/**
 * this the model class will load the date and save it
 * @param context the application context
 */
class MainStorageModel(val context: Context) : StorageModel {

    override fun saveTreeUri(treeUri: Uri, sdCardName: String) {
        val spE = context
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE).edit()
        spE.putString(TREE_URI_ + sdCardName, treeUri.toString())
        spE.apply()
    }

    override fun getTreeUri(storageName: String): Uri {
        val sp = context.getSharedPreferences(
            SD_CARD_TREE_URI_SP,
            AppCompatActivity.MODE_PRIVATE
        )
        return sp.getString(TREE_URI_ + storageName, "")!!.toUri()
    }

    override fun viewSortBySettings(): String? {
        val sp =
            context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getString(SHARED_PREFERENCES_SORTING_BY, DEFAULT_SORTING_ARGUMENT)
    }

    override fun viewSortOrderSettings(): String? {
        val sp =
            context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getString(SHARED_PREFERENCES_SORTING_ORDER, DEFAULT_SORTING_ORDER)
    }

    override fun viewHiddenFilesSettings(): Boolean {
        val sp =
            context.getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        return sp.getBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, DEFAULT_SHOW_HIDDEN_FILES)
    }

    override fun saveViewingSettings(sortBy: String, order: String, showHiddenFiles: Boolean) {
        val sharedPreferencesEditor = context.getSharedPreferences(
            VIEW_SETTINGS_SHARED_PREFERENCES,
            AppCompatActivity.MODE_PRIVATE
        ).edit()
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_BY, sortBy)
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_ORDER, order)
        sharedPreferencesEditor.putBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, showHiddenFiles)
        sharedPreferencesEditor.apply()
    }
}