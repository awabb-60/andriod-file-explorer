package com.awab.fileexplorer.presenter

import android.content.Context
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.presenter.contract.FilesListPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.view.contract.IFileFragmentView
import java.io.File

class FilePresenter(
    override val view: IFileFragmentView,
    private val folder:File,
    private val mStoragePresenter: StoragePresenterContract
): FilesListPresenterContract {

    lateinit var list:List<FileModel>

    override val filesList: List<FileModel>
        get() = list

    override val actionModeOn: Boolean
        get() = mStoragePresenter.actionModeOn


    override fun loadFiles() {
        val sp = view.context().getSharedPreferences(VIEW_TYPE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val type = sp.getString(SHARED_PREFERENCES_SORTING_TYPE, SORTING_TYPE_NAME)!!
        val order = sp.getString(SHARED_PREFERENCES_SORTING_ORDER, SORTING_ORDER_DEC)!!
        val list = makeFilesList(folder, sortingType = type, sortingOrder = order)
        this.list = list
        view.updateList(list)
    }

    override fun removeBreadcrumb() {
        mStoragePresenter.removeBreadcrumb()
    }

    override fun onFileClick(file: FileModel) {
        mStoragePresenter.onFileClicked(file)
    }

    override fun onFileLongClick(file: FileModel) {
        mStoragePresenter.onFileLongClicked(file)
    }

    override fun selectOrUnClickedItem(file: FileModel) {
        view.selectOrUnSelectItem(file)
    }

    override fun selectAll() {
        view.selectAll()
    }

    override fun getSelectedItems():List<FileModel> {
        return view.getSelectedItems()
    }

    override fun stopActionMode() {
        view.stopActionMode()
    }
}