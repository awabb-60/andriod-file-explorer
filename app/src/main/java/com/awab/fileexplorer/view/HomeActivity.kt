package com.awab.fileexplorer.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.model.data_models.StorageModel
import com.awab.fileexplorer.adapters.StoragesAdapter
import com.awab.fileexplorer.presenter.HomePresenter
import com.awab.fileexplorer.databinding.ActivityHomeBinding
import com.awab.fileexplorer.model.utils.storageAccess
import com.awab.fileexplorer.view.contract.HomeView

class HomeActivity : AppCompatActivity(), HomeView {
    private val TAG = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mHomePresenter: HomePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mHomePresenter = HomePresenter(this)

        storageAccess(this)

        val mStorageAdapter = StoragesAdapter {
            mHomePresenter.openStorage(it)
        }

//        val adapter = StoragesAdapter(this, ArrayList())
//        binding.lvListView.adapter = adapter
//
//        getStorages().forEach {
//            adapter.add(it)
//        }

        binding.rvStorages.adapter = mStorageAdapter.apply {
            set(getStorages())
        }

        binding.rvStorages.layoutManager = LinearLayoutManager(this)
        binding.rvStorages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.rvStorages.setHasFixedSize(true)

        binding.btnMediaImages.setOnClickListener{
            mHomePresenter.mediaItemClicked(it.id)
        }
        binding.btnMediaVideo.setOnClickListener{
            mHomePresenter.mediaItemClicked(it.id)
        }
        binding.btnMediaAudio.setOnClickListener{
            mHomePresenter.mediaItemClicked(it.id)
        }
        binding.btnMediaDocs.setOnClickListener{
            mHomePresenter.mediaItemClicked(it.id)
        }
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

    private fun getStorages(): List<StorageModel> {
        val storagesPaths = ContextCompat.getExternalFilesDirs(this, null)
        val storages = storagesPaths.map {
            it?.parentFile?.parentFile?.parentFile?.parentFile
        }
        return mHomePresenter.makeStoragesModels(storages)
    }
}