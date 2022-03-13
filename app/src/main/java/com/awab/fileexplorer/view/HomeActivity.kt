package com.awab.fileexplorer.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.databinding.ActivityHomeBinding
import com.awab.fileexplorer.presenter.HomePresenter
import com.awab.fileexplorer.presenter.contract.HomePresenterContract
import com.awab.fileexplorer.utils.adapters.PinedFilesAdapter
import com.awab.fileexplorer.utils.adapters.StoragesAdapter
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.utils.storageAccess
import com.awab.fileexplorer.view.contract.HomeView

class HomeActivity : AppCompatActivity(), HomeView {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mHomePresenter: HomePresenterContract
    private lateinit var mStorageAdapter: StoragesAdapter
    private lateinit var mPinedFilesAdapter: PinedFilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mHomePresenter = HomePresenter(this)

        // ask the user for the storage permissions
        storageAccess(this)

        binding.btnTesting.setOnClickListener {
            mHomePresenter.loadPinedFiles()
        }

        // make the pined files adapter
        mPinedFilesAdapter = PinedFilesAdapter(this)
        binding.rvRecentFiles.adapter = mPinedFilesAdapter
        binding.rvRecentFiles.layoutManager = GridLayoutManager(this, PinedFilesAdapter.PER_ROW)
        binding.rvRecentFiles.setHasFixedSize(true)

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
            listOf(btnMediaImages, btnMediaVideo, btnMediaAudio, btnMediaDocs).forEach {mediaButton->
                mediaButton.setOnClickListener {
                    mHomePresenter.mediaItemClicked(mediaButton.id)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // to update the list at the start of the activity and after navigating back from the storage or media
        // activities
        mHomePresenter.loadStorages()
        mHomePresenter.loadPinedFiles()
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

    override fun updateStoragesList(storages: Array<StorageDataModel>) {
        mStorageAdapter.submitList(storages)
    }

    override fun updatePinedFilesList(list: List<FileDataModel>) {
        mPinedFilesAdapter.submitList(list)
    }
}