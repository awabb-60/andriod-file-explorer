package com.awab.fileexplorer.view.action_mode_callbacks

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.awab.fileexplorer.R
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract

class StorageActionModeCallBack(private val presenterContract: StoragePresenterContract):ActionMode.Callback{
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.storage_action_mode_menu, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        if (presenterContract.shouldStopActionMode()) {
            onDestroyActionMode(mode)
            return false
        }
        mode?.title = presenterContract.getActionModeTitle()
        menu?.findItem(R.id.miRename)?.isVisible = presenterContract.showMIRename()
        menu?.findItem(R.id.miOpenWith)?.isVisible = presenterContract.showMIOpenWith()
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.miDelete-> presenterContract.confirmDelete()
            R.id.miMove-> presenterContract.startMoveScreen()
            R.id.miCopy-> presenterContract.startCopyScreen()
            R.id.miRename-> presenterContract.confirmRename()
            R.id.miOpenWith-> presenterContract.openWith()
            R.id.miDetails-> presenterContract.showDetails()
            R.id.miSelectAll-> presenterContract.selectAll()
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        presenterContract.stopActionMode()
    }
}