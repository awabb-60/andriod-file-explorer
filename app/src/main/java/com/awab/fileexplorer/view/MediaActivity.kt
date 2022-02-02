package com.awab.fileexplorer.view

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.adapters.MediaAdapter
import com.awab.fileexplorer.databinding.ActivityMediaBinding
import com.awab.fileexplorer.databinding.ItemDetailsLayoutBinding
import com.awab.fileexplorer.databinding.ItemsDetailsLayoutBinding
import com.awab.fileexplorer.databinding.LoadingLayoutBinding
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.presenter.MediaPresenter
import com.awab.fileexplorer.presenter.contract.MediaPresenterContract
import com.awab.fileexplorer.view.action_mode_callbacks.MediaActionModeCallback
import com.awab.fileexplorer.view.contract.MediaView

class MediaActivity : AppCompatActivity(), MediaView {

    private lateinit var binding: ActivityMediaBinding
    private lateinit var presenter: MediaPresenterContract

    private lateinit var adapter: MediaAdapter

    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBackButton.setOnClickListener {
            onBackPressed()
        }

        setSupportActionBar(binding.toolBar)

        title = ""

        adapter = MediaAdapter().apply {
            setContext(this@MediaActivity)
        }

        binding.rvMedia.adapter = adapter
        binding.rvMedia.layoutManager = LinearLayoutManager(this)
        binding.rvMedia.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.rvMedia.setHasFixedSize(true)

        presenter = MediaPresenter(this)
        presenter.loadMedia(intent)

        adapter.setPresenter(presenter)
    }

    override val mediaAdapter: MediaAdapter
        get() = adapter

    override fun context(): Context {
        return this
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showDetails(name: String, path: String, size: String, dateStr: String) {
        val dialogBinding = ItemDetailsLayoutBinding.inflate(layoutInflater)

        dialogBinding.apply {
            tvDetailsName.text = name
            tvDetailsPath.text = path
            tvDetailsSize.text = size
            tvDetailslastModified.text = dateStr
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Details")
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        dialogBinding.tvOk.setOnClickListener {
            dialog.cancel()
        }
    }

    override fun showDetails(contains: String, totalSize: String) {
        val dialogBinding = ItemsDetailsLayoutBinding.inflate(layoutInflater)

        dialogBinding.apply {
            tvDetailsContains.text = contains
            tvDetailsTotalSize.text = totalSize
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Details")
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        dialogBinding.tvOk.setOnClickListener {
            dialog.cancel()
        }
    }

    override fun openFile(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "no app can open this file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun startActionMode() {
        actionMode = startSupportActionMode(MediaActionModeCallback(presenter))
    }

    override fun updateActionMode() {
        actionMode?.invalidate()
    }

    override fun stopActionMode() {
        actionMode = null
    }

    override fun pressBack() {
        actionMode?.finish()
    }
}