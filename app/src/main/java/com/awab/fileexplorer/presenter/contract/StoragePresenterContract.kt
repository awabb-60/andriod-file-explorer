package com.awab.fileexplorer.presenter.contract

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.RecentFiles
import com.awab.fileexplorer.model.types.FileType
import com.awab.fileexplorer.model.types.MimeType
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File
import com.awab.fileexplorer.model.utils.*
import java.text.SimpleDateFormat

interface StoragePresenterContract {

    // the name of your sd card
    val sdCardName: String
        get() = ""

    // the path of your sd card
    val sdCardPath: String
        get() = ""

    // the path of your sd card
    val internalStoragePath: String
        get() = ""

    var filesListPresenter: FilesListPresenterContract?

    var actionModeOn:Boolean

    val view: StorageView

    /**
     * to check is the storage is authorized
     * mainly for sd card
     */
    fun isAuthorized():Boolean

    fun saveTreeUri(treeUri: Uri)

    fun requestPermission()

    fun isValidTreeUri(treeUri: Uri):Boolean

    fun createFolder(path:String)

    fun confirmDelete(){
        view.confirmDelete()
    }

    fun delete()

    fun getMIRenameVisibility():Boolean{
//        true to show rename
        val count = filesListPresenter?.filesList?.count { it.selected }?:return false
        return count ==1
    }
    fun removeBreadcrumb(){
        view.removeBreadcrumb()
    }

    fun getSelectedTitle():String{
        val count = filesListPresenter?.filesList?.count { it.selected }?:return ""

        return if (count <= 1)
            "$count item Selected"
        else
            "$count items Selected"
    }

    fun selectAll(){
        filesListPresenter?.selectAll()
        view.updateActionMode()
    }

    fun shouldStopActionMode():Boolean{
        val count = filesListPresenter?.filesList?.count { it.selected }?:return true
        return count <= 0
    }

    fun stopActionMode(){
        actionModeOn = false
        view.stopActionMode()
        filesListPresenter?.stopActionMode()
    }

    fun showDetails(){
        val selectedList = filesListPresenter?.getSelectedItems()

        if (selectedList?.size == 1){
            val item = selectedList[0]
            val date = SimpleDateFormat(DATE_FORMAT_PATTERN).format(item.date)
            if (item.type == FileType.FILE){
                view.showItemDetails(item.name, date, item.size, item.path)
            } else{
                val sizeInBytes = getFolderSizeBytes(File(item.path))
                val size = getSize(sizeInBytes)
                view.showItemDetails(item.name, date, size, item.path)
            }
        }else{
            val contains = getContains(selectedList)
            val totalSize = getTotalSize(selectedList)
            view.showItemsDetails(contains, totalSize)
        }
    }

    fun getTotalSize(selectedList: List<FileModel>?): String{
        var totalSizeBytes = 0L
        selectedList?.forEach {
            totalSizeBytes += if (it.type == FileType.FILE){
                File(it.path).length()
            }else
                getFolderSizeBytes(File(it.path))
        }
        return getSize(totalSizeBytes)
    }

    fun getContains(selectedList: List<FileModel>?): String {
        var fileCount = 0
        var folderCount = 0
        selectedList?.forEach {
            if (it.type == FileType.FILE)
                fileCount ++
            else{
                folderCount ++
                fileCount += getInnerFilesCount(File(it.path))
                folderCount += getInnerFoldersCount(File(it.path))
            }
        }
        return "$fileCount Files, $folderCount Folders"
    }

    fun confirmRename(){
        val item = filesListPresenter?.getSelectedItems()?.get(0)
        if (item != null)
            view.showRenameDialog(item.path, item.name)
    }

    fun rename(path:String, newName:String)

    fun onFileClicked(file: FileModel){
//        selecting unselecting the item
        if (actionModeOn){
            filesListPresenter?.selectOrUnClickedItem(file)
            view.updateActionMode()
            return
        }

//        opining the item
        if (file.type == FileType.FILE) {
//            file cant be opened
        if (file.mimeType == MimeType.UNKNOWN) {
            Toast.makeText(view.context(), "unsupported file format", Toast.LENGTH_SHORT).show()
            return
        }
//            opening the file
        view.openFile(getOpenFileIntent(file))
    } else // navigating to folder
        view.navigateToFolder(file.name, file.path)
}

    fun onFileLongClicked(file: FileModel){
//        starting the action mode
       if (!actionModeOn){
           actionModeOn = true
           filesListPresenter?.selectOrUnClickedItem(file)
           view.startActionMode()
       }else{ // selecting unselecting the item
           filesListPresenter?.selectOrUnClickedItem(file)
           view.updateActionMode()
       }
}

    fun getOpenFileIntent(file: FileModel): Intent {
        RecentFiles.recentFilesList.add(file.path)
        if (file.mimeType == MimeType.APPLICATION) {
            return Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(file.uri, file.mimeType.mimeString)
            }
        }
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, file.mimeType.mimeString)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun startCopyScreen() {
        val selectedItem = filesListPresenter?.getSelectedItems()
        if (selectedItem == null || selectedItem.isEmpty())
            return
        view.startCopyScreen()
    }

    fun startMoveScreen() {
        val selectedItem = filesListPresenter?.getSelectedItems()
        if (selectedItem == null || selectedItem.isEmpty())
            return
        view.startMoveScreen()
    }

    // to copy or move to the sd card or the internal storage, you have to have a folder
    // named (Paste) in location you try to copy to.
    fun copy(to:String) {
        val selectedItem = filesListPresenter?.getSelectedItems()
        if (selectedItem == null || selectedItem.isEmpty())
            return

        val listPath = selectedItem.map { it.path }
        view.openCopyProgress("Copying")

        val intent = when (to) {
            "I" -> {
                Intent(COPY_BROADCAST_ACTION).apply {
                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    //  the copy location
                    putExtra(PASTE_LOCATION, internalStoragePath + File.separator +"Paste")
                }
            }
            "S" ->{
                Intent(COPY_BROADCAST_ACTION).apply {
                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    //  the copy location
                    putExtra(PASTE_LOCATION, sdCardPath + File.separator + "Paste")
                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
                }
            }

            else-> Intent("Noop")
        }

        intent.putExtra(COPY_TYPE_EXTRA, COPY)
//        starting the copy service
        view.context().sendBroadcast(intent)
    }

    fun move(to:String){
        val selectedItem = filesListPresenter?.getSelectedItems()
        if (selectedItem == null || selectedItem.isEmpty())
            return

        val listPath = selectedItem.map { it.path }
        view.openCopyProgress("Moving")

        val intent = when (to) {
            "I" -> {
                Intent(COPY_BROADCAST_ACTION).apply {
                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    //  the copy location
                    putExtra(PASTE_LOCATION, internalStoragePath + File.separator +"Paste")
                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
                }
            }
            "S" ->{
                Intent(COPY_BROADCAST_ACTION).apply {
                    putExtra(COPY_PATHS_EXTRA, arrayOf(*listPath.toTypedArray()))
                    putExtra(PASTE_LOCATION, sdCardPath + File.separator + "Paste")
                    putExtra(TREE_URI_FOR_COPY_EXTRA, getTreeUri(view.context(), sdCardName).toString())
                    putExtra(EXTERNAL_STORAGE_PATH_EXTRA, sdCardPath)
                }
            }

            else-> Intent("Noop")
        }

        intent.putExtra(COPY_TYPE_EXTRA, MOVE)
//        starting the copy service
        view.context().sendBroadcast(intent)
    }

    fun getTreeUri(context: Context, storageName: String): Uri {
        val sp = context.getSharedPreferences(
            SD_CARD_TREE_URI_SP,
            AppCompatActivity.MODE_PRIVATE
        )
        return sp.getString(TREE_URI_ + storageName, "")!!.toUri()
    }

    fun updateCopyProgress(p:Int) {
        view.updateCopyProgress(p, 10,"","")
    }

    fun cancelCopy() {
        CopyServices.cancelCopy()
        view.stopCloseCopyScreen()
        stopActionMode()
        filesListPresenter?.loadFiles()
        Toast.makeText(view.context(), "copy done", Toast.LENGTH_SHORT).show()
    }

}