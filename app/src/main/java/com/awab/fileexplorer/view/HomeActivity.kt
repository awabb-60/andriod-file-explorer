package com.awab.fileexplorer.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.databinding.ActivityHomeBinding
import com.awab.fileexplorer.databinding.ItemDetailsLayoutBinding
import com.awab.fileexplorer.presenter.HomePresenter
import com.awab.fileexplorer.presenter.contract.HomePresenterContract
import com.awab.fileexplorer.utils.adapters.QuickAccessAdapter
import com.awab.fileexplorer.utils.adapters.StoragesAdapter
import com.awab.fileexplorer.utils.data.data_models.QuickAccessFileDataModel
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.utils.data.data_models.Tap
import com.awab.fileexplorer.utils.storageAccess
import com.awab.fileexplorer.view.contract.HomeView
import com.awab.fileexplorer.view.custom_views.CustomDialog

class HomeActivity : AppCompatActivity(), HomeView {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mHomePresenter: HomePresenterContract
    private lateinit var mStorageAdapter: StoragesAdapter
    private lateinit var mQuickAccessFilesAdapter: QuickAccessAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ::binding.isInitialized

        mHomePresenter = HomePresenter(this)

        // ask the user for the storage permissions
        storageAccess(this)

        binding.btnTesting.setOnClickListener {
            mHomePresenter.loadPinedFiles()
        }

        val pinedFilesTap = Tap("Pined Files") { mHomePresenter.loadPinedFiles() }
        binding.tapsLayout.addTap(pinedFilesTap)

        val recentFilesTap = Tap("Recent Files") { mHomePresenter.loadRecentFiles() }
        binding.tapsLayout.addTap(recentFilesTap)

        // make the pined files adapter
        mQuickAccessFilesAdapter = QuickAccessAdapter(this, mHomePresenter)
        binding.rvQuickAccess.adapter = mQuickAccessFilesAdapter
        binding.rvQuickAccess.layoutManager = GridLayoutManager(this, QuickAccessAdapter.PER_ROW)
        binding.rvQuickAccess.setHasFixedSize(true)

        binding.btnEditQuickAccess.setOnClickListener {
            mHomePresenter.quickAccessInEditMode = true
            it.visibility = View.GONE
            mQuickAccessFilesAdapter.startEditMode()
        }

        // make the storage adapter
        mStorageAdapter = StoragesAdapter {
            mHomePresenter.openStorage(it)
        }

        binding.rvStorages.adapter = mStorageAdapter
        binding.rvStorages.layoutManager = LinearLayoutManager(this)
        binding.rvStorages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.rvStorages.setHasFixedSize(true)

        // setting click listeners for the media items
        binding.apply {
            listOf(btnMediaImages, btnMediaVideo, btnMediaAudio, btnMediaDocs).forEach { mediaButton ->
                mediaButton.setOnClickListener {
                    mHomePresenter.mediaItemClicked(mediaButton.id)
                }
            }
        }
    }

    override fun onBackPressed() {
        // closing the edit mode in the quick access window
        if (mHomePresenter.quickAccessInEditMode) {
            mHomePresenter.quickAccessInEditMode = false
            binding.btnEditQuickAccess.isVisible = true
            mQuickAccessFilesAdapter.stopEditMode()
        } else
            super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        // to update the list at the start of the activity and after navigating back from the storage or media
        // activities
        mHomePresenter.loadStorages()
        binding.tapsLayout.refreshCurrentTap()
    }

    override fun checkForPermissions() {
        storageAccess(this)
    }

    override fun context(): Context {
        return this
    }

    override fun openActivity(intent: Intent) {
        startActivity(intent)
    }

    override fun showDetailsDialog(name: String, size: String, lastModified: String, path: String) {
        val dialogBinding = ItemDetailsLayoutBinding.inflate(layoutInflater)
        dialogBinding.tvDetailsName.text = name
        dialogBinding.tvDetailsLastModified.text = lastModified
        dialogBinding.tvDetailsSize.text = size
        dialogBinding.tvDetailsPath.text = path
        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)
        dialog.show()
        dialogBinding.buttonsLayout.addButton("Ok") { dialog.cancel() }
        dialogBinding.buttonsLayout.addButton("Locate") {
            dialog.cancel()
            mHomePresenter.locateFile(path)
        }
    }

    override fun updateStoragesList(storages: Array<StorageDataModel>) {
        mStorageAdapter.submitList(storages)
    }

    override fun updateQuickAccessFilesList(list: List<QuickAccessFileDataModel>) {
        binding.rvQuickAccess.visibility = View.VISIBLE
        binding.quickAccessEmptyMessage.visibility = View.GONE
        mQuickAccessFilesAdapter.submitList(list)
    }

    override fun quickAccessIsEmpty() {
        binding.rvQuickAccess.visibility = View.GONE
        binding.quickAccessEmptyMessage.visibility = View.VISIBLE
    }

    override fun updateQuickAccessWindowHeight(cardHeight: Int) {
        binding.quickAccessFilesCard.layoutParams.height = cardHeight
    }

    override fun showEditQuickAccess() {
        binding.btnEditQuickAccess.visibility = View.VISIBLE
    }
}