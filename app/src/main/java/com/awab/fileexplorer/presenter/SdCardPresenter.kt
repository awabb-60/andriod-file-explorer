package com.awab.fileexplorer.presenter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.awab.fileexplorer.presenter.contract.FilesListPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.model.utils.PICKER_REQUEST_CODE
import com.awab.fileexplorer.model.utils.SD_CARD_TREE_URI_SP
import com.awab.fileexplorer.model.utils.TREE_URI_
import com.awab.fileexplorer.presenter.contract.SearchPresenterContract
import com.awab.fileexplorer.view.contract.StorageView

class SdCardPresenter(
    private val storageView: StorageView,
    private val storageName: String,
    private val storagePath: String
) : StoragePresenterContract {

    val copyLocation = ""

    override lateinit var filesListPresenter: FilesListPresenterContract

    override lateinit var searchPresenter: SearchPresenterContract

    override var actionModeOn: Boolean = false

    override val view: StorageView
        get() = storageView

    override fun isAuthorized(): Boolean {
        return sdCardAuthorized()
    }

    override fun requestPermission() {
        Toast.makeText(view.context(), "select the sd card", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            openPicker()
    }

    override fun rename(path: String, newName: String) {
        try {
            val oldName = path.split('/').last()


            val parentDir = getTreeUriFile()
            val file = navigateToParentTreeFile(parentDir, path)
            if (file != null) {
                val success = file.findFile(oldName)?.renameTo(newName)
//                    some error occur... the file was node found or couldn't delete this file
                if (success == null || !success)
                    Toast.makeText(view.context(), "error renaming $oldName", Toast.LENGTH_SHORT).show()
            } else { //  error navigating to the file
                Toast.makeText(view.context(), "error renaming $oldName", Toast.LENGTH_SHORT).show()
            }

            view.stopActionMode()
            filesListPresenter?.loadFiles()
        } catch (e: Exception) {
            Toast.makeText(view.context(), "IO error occurred", Toast.LENGTH_SHORT).show()
        }

    }

    override fun createFolder(path: String) {
        try {
//        cutting the parent directory path from sd card path
            val newFolderName = path.split('/').last().trim()

            val parentDir = getTreeUriFile()

//        navigating to the parent
            val file = navigateToParentTreeFile(parentDir, path)

//        creating the new directory and refreshing
            if (file != null) {
                val success = file.createDirectory(newFolderName)
                if (success != null)
                    filesListPresenter?.loadFiles()
                else
                    Toast.makeText(view.context(), "unable to create this file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(view.context(), "unable to create this file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun delete() {
        try {
            val selectedItems = filesListPresenter?.getSelectedItems()
            val parentDir = getTreeUriFile()
            if (selectedItems == null || selectedItems.isEmpty())
                return
            val file = navigateToParentTreeFile(parentDir, selectedItems[0].path)
            selectedItems.forEach {
                if (file != null) {
                    val success = file.findFile(it.name)?.delete()
//                    some error occur... the file was node found or couldn't delete this file
                    if (success == null || !success)
                        Toast.makeText(view.context(), "error deleting ${it.name}", Toast.LENGTH_SHORT).show()
                } else
//                    error navigating to the file
                    Toast.makeText(view.context(), "error deleting ${it.name}", Toast.LENGTH_SHORT).show()
            }
            view.stopActionMode()
            filesListPresenter?.loadFiles()
        } catch (e: Exception) {
            Toast.makeText(view.context(), "error deleting files", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTreeUriFile(): DocumentFile? {
        return DocumentFile.fromTreeUri(view.context(), getTreeUri(view.context(), storageName))
    }

    private fun navigateToParentTreeFile(treeDocumentFile: DocumentFile?, filePath: String): DocumentFile? {
//        removing the name from the path
        var innerPath = filePath.dropLastWhile { it != '/' }

//        removing the tree storage name  (sd card)
        innerPath = innerPath.removeRange(0, filePath.indexOf(storageName) + storageName.length + 1)

        var file = treeDocumentFile
//        navigating to it parent
        for (fileName in innerPath.split('/').filter { it != "" }) {
            file = file?.findFile(fileName)
            if (file == null)
                break
        }
        return file
    }

    @RequiresApi(19)
    private fun openPicker() {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        view.openAuthorizationPicker(intent, PICKER_REQUEST_CODE)
    }

    override fun isValidTreeUri(treeUri: Uri): Boolean {
        DocumentFile.fromTreeUri(view.context(), treeUri)?.name?.let { sdCardName ->
            return sdCardName == storageName
        }
        return false
    }

    private fun sdCardAuthorized(): Boolean {
        val sp = view.context()
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE)
        return sp.contains(TREE_URI_ + storageName)
    }

    override fun saveTreeUri(treeUri: Uri) {
        val spE = view.context()
            .getSharedPreferences(SD_CARD_TREE_URI_SP, AppCompatActivity.MODE_PRIVATE).edit()
        spE.putString(TREE_URI_ + storageName, treeUri.toString())
        spE.apply()
    }


}