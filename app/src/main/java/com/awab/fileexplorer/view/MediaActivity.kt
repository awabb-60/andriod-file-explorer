package com.awab.fileexplorer.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.R
import com.awab.fileexplorer.databinding.ActivityMediaBinding
import com.awab.fileexplorer.databinding.FileDetailsLayoutBinding
import com.awab.fileexplorer.databinding.FilesDetailsLayoutBinding
import com.awab.fileexplorer.presenter.MediaPresenter
import com.awab.fileexplorer.presenter.contract.MediaPresenterContract
import com.awab.fileexplorer.utils.adapters.MediaAdapter
import com.awab.fileexplorer.view.action_mode_callbacks.MediaActionModeCallback
import com.awab.fileexplorer.view.contract.MediaView
import com.awab.fileexplorer.view.custom_views.CustomDialog

class MediaActivity : AppCompatActivity(), MediaView, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private lateinit var binding: ActivityMediaBinding
    private lateinit var presenter: MediaPresenterContract
    private lateinit var adapter: MediaAdapter

    private var actionMode: ActionMode? = null

    private lateinit var _loadingDialog:AlertDialog
    override val loadingDialog: AlertDialog
        get() = _loadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)

        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        presenter = MediaPresenter(this)

        adapter = MediaAdapter(this, presenter)

        binding.rvMedia.adapter = adapter
        binding.rvMedia.layoutManager = LinearLayoutManager(this)
        binding.rvMedia.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.rvMedia.setHasFixedSize(true)

        presenter.loadFiles(intent)
        _loadingDialog = CustomDialog.makeLoadingDialog(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.cancelLoadFiles()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.media_main_menu, menu)

        val searchItem = menu?.findItem(R.id.miMediaSearch)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener(this)
        searchView.queryHint = getString(R.string.search_hint)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null)
            presenter.searchTextChanged(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null)
            presenter.searchTextChanged(newText)
        return false
    }

    override fun onClose(): Boolean {
        presenter.loadFiles(intent)
        return false
    }

    override val mediaAdapter: MediaAdapter
        get() = adapter

    override fun context(): Context {
        return this
    }

    override fun setTitle(title: String) {
        this.title = title
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showDetails(name: String, path: String, size: String, dateStr: String) {
        val dialogBinding = FileDetailsLayoutBinding.inflate(layoutInflater)

        dialogBinding.apply {
            tvDetailsName.text = name
            tvDetailsPath.text = path
            tvDetailsSize.text = size
            tvDetailsLastModified.text = dateStr
        }

        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)

        dialog.show()

        dialogBinding.buttonsLayout.addButton("Ok") { dialog.cancel() }
    }

    override fun showDetails(contains: String, totalSize: String) {
        val dialogBinding = FilesDetailsLayoutBinding.inflate(layoutInflater)

        dialogBinding.apply {
            tvDetailsContain.text = contains
            tvDetailsTotalSize.text = totalSize
        }

        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)

        dialog.setTitle("Details")

        dialog.show()
        dialogBinding.buttonsLayout.addButton("Ok") { dialog.cancel() }
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

    override fun finishActionMode() {
        actionMode?.finish()
    }
}