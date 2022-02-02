package com.awab.fileexplorer.view.action_mode_callbacks

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.awab.fileexplorer.R
import com.awab.fileexplorer.presenter.contract.MediaPresenterContract

class MediaActionModeCallback(private val presenter: MediaPresenterContract) : ActionMode.Callback {
    private val TAG = "MediaActionModeCallback"
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.media_action_mode_menu, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.title = presenter.getActionModeTitle()
        menu?.findItem(R.id.miMediaOpenWith)?.isVisible = presenter.oneItemSelected()
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.miMediaDetails -> {
                presenter.showDetails()
                true
            }
            R.id.miMediaSelectAll -> {
                presenter.selectAll()
                true
            }
            R.id.miMediaOpenWith -> {
                presenter.openWith()
                true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        presenter.stopActionMode()
    }
}