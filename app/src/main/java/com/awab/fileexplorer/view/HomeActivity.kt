package com.awab.fileexplorer.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.adapters.StoragesAdapter
import com.awab.fileexplorer.databinding.ActivityHomeBinding
import com.awab.fileexplorer.model.data_models.StorageModel
import com.awab.fileexplorer.model.utils.TRANSFER_REQUEST_CODE
import com.awab.fileexplorer.model.utils.storageAccess
import com.awab.fileexplorer.presenter.HomePresenter
import com.awab.fileexplorer.presenter.contract.HomePresenterContract
import com.awab.fileexplorer.view.contract.HomeView

class HomeActivity : AppCompatActivity(), HomeView {
    private val TAG = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding
    private lateinit var mHomePresenter: HomePresenterContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mHomePresenter = HomePresenter(this)

        storageAccess(this)

        val mStorageAdapter = StoragesAdapter {
            mHomePresenter.openStorage(it)
        }

        binding.rvStorages.adapter = mStorageAdapter.apply {
            set(getStorages())
        }

        binding.rvStorages.layoutManager = LinearLayoutManager(this)
        binding.rvStorages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.rvStorages.setHasFixedSize(true)

        binding.btnMediaImages.setOnClickListener {
            mHomePresenter.mediaItemClicked(it.id)
        }
        binding.btnMediaVideo.setOnClickListener {
            mHomePresenter.mediaItemClicked(it.id)
        }
        binding.btnMediaAudio.setOnClickListener {
            mHomePresenter.mediaItemClicked(it.id)
        }
        binding.btnMediaDocs.setOnClickListener {
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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == TRANSFER_REQUEST_CODE && resultCode == RESULT_OK) {
//            if (data != null) {
//                val b = data.getBundleExtra("T_DATA")
//                Toast.makeText(this, "$b", Toast.LENGTH_SHORT).show()
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }

    private fun getStorages(): List<StorageModel> {
        return mHomePresenter.makeStoragesModels()
    }
}