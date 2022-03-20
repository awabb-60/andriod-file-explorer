package com.awab.fileexplorer.presenter

import android.content.Context
import com.awab.fileexplorer.model.utils.makeFilesList
import com.awab.fileexplorer.presenter.contract.FilesListPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.utils.*
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.view.contract.IFileFragmentView
import java.io.File

class FilePresenter(
    override val view: IFileFragmentView,
    private val folder: File,
    private val mStoragePresenter: StoragePresenterContract
) : FilesListPresenterContract {

    override val mainStoragePresenter: StoragePresenterContract
        get() = mStoragePresenter

    override fun removeBreadcrumb() {
        mainStoragePresenter.removeBreadcrumb()
    }

    override fun loadFiles() {
        val sp = view.context().getSharedPreferences(VIEW_SETTINGS_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val sortBy = sp.getString(SHARED_PREFERENCES_SORTING_BY, SORTING_BY_NAME)!!
        val order = sp.getString(SHARED_PREFERENCES_SORTING_ORDER, SORTING_ORDER_DEC)!!
        val showHiddenFiles = sp.getBoolean(SHARED_PREFERENCES_SHOW_HIDDEN_FILES, false)
        val list =
            makeFilesList(folder, sortingBy = sortBy, sortingOrder = order, showHidingFiles = showHiddenFiles)
        view.updateList(list)
    }

    override fun onFileClick(file: FileDataModel) {
        mainStoragePresenter.onFileClicked(file)
    }

    override fun onFileLongClick(file: FileDataModel) {
        mainStoragePresenter.onFileLongClicked(file)
    }

    override fun selectOrUnSelectItem(file: FileDataModel) {
        view.selectOrUnSelectItem(file)
    }

    override fun selectAll() {
        view.selectAll()
    }

    override fun getAllItems(): List<FileDataModel> {
        return view.getAllItems()
    }

    override fun getSelectedItems(): List<FileDataModel> {
        return view.getSelectedItems()
    }

    override fun getSelectedItemCount(): Int {
        return view.getSelectedItems().count()
    }

    override fun stopActionMode() {
        view.stopActionMode()
    }

//    override fun locate(file: FileDataModel) {
//        view.getItems().forEachIndexed { index, fileDataModel ->
//            if (file == fileDataModel)
//                view.scrollTo(index)
//        }
//    }
}