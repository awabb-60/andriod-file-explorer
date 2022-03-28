package com.awab.fileexplorer.presenter

import com.awab.fileexplorer.model.MainStorageModel
import com.awab.fileexplorer.presenter.contract.FilesListPresenterContract
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.makeFilesList
import com.awab.fileexplorer.view.contract.IFileFragmentView
import java.io.File

class FilePresenter(
    override val view: IFileFragmentView,
    private val folder: File,
    private val mStoragePresenter: StoragePresenterContract
) : FilesListPresenterContract {

    override val mainStoragePresenter: StoragePresenterContract
        get() = mStoragePresenter

    override val model = MainStorageModel(view.context())

    override fun removeBreadcrumb() {
        mainStoragePresenter.removeBreadcrumb()
    }

    override fun loadFiles() {
        val files = makeFilesList(
            folder,
            model.viewSortBySettings(), model.viewSortOrderSettings(), model.viewHiddenFilesSettings()
        )
        view.updateList(files)
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
}