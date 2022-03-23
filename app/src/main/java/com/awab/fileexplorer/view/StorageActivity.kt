package com.awab.fileexplorer.view

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.*
import com.awab.fileexplorer.databinding.*
import com.awab.fileexplorer.presenter.*
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.utils.*
import com.awab.fileexplorer.utils.adapters.BreadcrumbsAdapter
import com.awab.fileexplorer.utils.data.data_models.BreadcrumbsDataModel
import com.awab.fileexplorer.utils.data.types.StorageType
import com.awab.fileexplorer.utils.listeners.BreadcrumbsListener
import com.awab.fileexplorer.utils.transfer_utils.CancelTransferBroadCast
import com.awab.fileexplorer.view.action_mode_callbacks.StorageActionModeCallBack
import com.awab.fileexplorer.view.contract.StorageView
import com.awab.fileexplorer.view.custom_views.CustomDialog
import com.awab.fileexplorer.view.custom_views.PickPasteLocationDialogFragment

class StorageActivity : AppCompatActivity(), BreadcrumbsListener, StorageView {
    private val TAG = "StorageActivity"
    private lateinit var binding: ActivityStorageBinding
    private lateinit var breadcrumbsAdapter: BreadcrumbsAdapter
    private lateinit var mStoragePresenter: StoragePresenterContract

    private lateinit var copyProgressReceiver: BroadcastReceiver
    private lateinit var finishCopyReceiver: BroadcastReceiver

    private var actionMode: ActionMode? = null

    private var progressDialogBinding: ProgressDialogLayoutBinding? = null
    private var progressDialog: AlertDialog? = null

    private lateinit var _loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        // this have to be before the super call
        // because when a configuration chang happen the fragments will get recreated again
        // the recreation happen is this supper call (not really sure)
        // so the storage presenter need to initialized before the fragments recreation
        createPresenter()
        super.onCreate(savedInstanceState)

        binding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // putting the toolbar as the support action bar
        setSupportActionBar(binding.selectToolBar)
        supportActionBar?.title = ""

//        preform a back button click when tha Navigation icon in the tool bar is clicked
        binding.selectToolBar.setNavigationOnClickListener {
            onBackPressed()
        }


        // adding the item listener to the mani menu
        binding.selectToolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.miCreateFolder -> mStoragePresenter.confirmCreateFolder()
                R.id.miView -> presenter.pickViewSettings()
                R.id.miSearch -> openSearchFragment()
            }
            true
        }

        // the breadcrumbs adapter will show the navigation map and the current folder location
        breadcrumbsAdapter = BreadcrumbsAdapter().apply {
            setListener(this@StorageActivity as BreadcrumbsListener)
        }
        binding.rvBreadcrumbs.adapter = breadcrumbsAdapter
        binding.rvBreadcrumbs.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        //  to close this activity when back pressed and no fragment are open
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0){
                finish()
            }
        }

        // opening the storage folder
        // only work at the beginning of the storage activity
        if (savedInstanceState == null) {
            presenter.start(intent)
        }

        // make the loading dialog
        _loadingDialog = CustomDialog.makeLoadingDialog(this)
    }

    private fun createPresenter() {
        val storagePath = intent.getStringExtra(STORAGE_PATH_EXTRA)!!

//        getting the presenter type... sd controller has more work
        val type = intent.getSerializableExtra(STORAGE_TYPE_EXTRA)!!
        if (type is StorageType) {
            when (type) {
                StorageType.INTERNAL -> {
                    mStoragePresenter = InternalStoragePresenter(this, storagePath)
                }
                StorageType.SDCARD -> {
                    mStoragePresenter = SdCardPresenterSAF(this, storagePath)
                }
            }
        } else { // this storage can be opened
            Toast.makeText(this, "cant open this storage", Toast.LENGTH_SHORT).show()
            finish()
        }
        if (::mStoragePresenter.isInitialized)
            mStoragePresenter.setViewSettings()
    }

    override val presenter: StoragePresenterContract
        get() = mStoragePresenter

    override val loadingDialog: AlertDialog
        get() = _loadingDialog

    override var showMenu = true

    override fun context(): Context {
        return this
    }

    override fun intent(): Intent {
        return intent
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun openFile(intent: Intent) {
        startActivity(intent)
    }

    override fun navigateToFolder(name: String, path: String) {
        val fileFragment = FilesFragment.newInstance(path)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fileFragment)
            .setTransition(TRANSIT_FRAGMENT_FADE)
            .addToBackStack(path)
            .commit()
        breadcrumbsAdapter.add(BreadcrumbsDataModel(name, path))
//        to scroll the adapter to the last breadcrumbs item
        binding.rvBreadcrumbs.smoothScrollToPosition(breadcrumbsAdapter.list.count() - 1)
    }

    override fun updateMenu() {
        invalidateOptionsMenu()
    }

    override fun removeBreadcrumb() {
        breadcrumbsAdapter.removeLast()
    }

    override fun openAuthorizationPicker(intent: Intent, requestCode: Int) {
        Toast.makeText(this, "select the sd card", Toast.LENGTH_LONG).show()
        startActivityForResult(intent, requestCode)
    }

    override fun startActionMode() {
        actionMode = startSupportActionMode(StorageActionModeCallBack(mStoragePresenter))
    }

    override fun updateActionMode() {
        actionMode?.invalidate()
    }

    override fun stopActionMode() {
        actionMode?.finish()
    }

    override fun showCreateFolderDialog() {
        val dirPath = breadcrumbsAdapter.list.last().path
        val dialogBinding = NamingFileLayoutBinding.inflate(layoutInflater, null, false)
        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)
        dialog.setTitle("Create new folder")
        dialog.show()

        // showing the keyboard
        dialogBinding.etNameFile.requestFocus()
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(dialogBinding.etNameFile, InputMethodManager.SHOW_IMPLICIT)

        dialogBinding.buttonsLayout.addButton("Save") {
            val etCreateFolderName = dialogBinding.etNameFile
            if (etCreateFolderName.text.isNotBlank()) {
                val name = etCreateFolderName.text.toString()
                mStoragePresenter.createFolder("$dirPath/$name")
            }
            dialog.cancel()
        }
        // adding the cancel button
        dialogBinding.buttonsLayout.addButton("Cancel") {
            dialog.cancel()
        }
    }

    override fun showRenameDialog(path: String, currentName: String) {
        val dialogBinding = NamingFileLayoutBinding.inflate(layoutInflater, null, false)
        dialogBinding.etNameFile.setText(currentName)

        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)


        dialog.setTitle("Rename File")
        dialog.show()

//        focusing on the edit text
        dialogBinding.etNameFile.requestFocus()
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(dialogBinding.etNameFile, InputMethodManager.SHOW_IMPLICIT)

        dialogBinding.buttonsLayout.addButton("Save"){
            if (dialogBinding.etNameFile.text.isNotBlank()) {
                val name = dialogBinding.etNameFile.text.toString().trim()
                mStoragePresenter.rename(path, name)
            } else
                Toast.makeText(this, "invalid name", Toast.LENGTH_SHORT).show()
            dialog.cancel()
        }
    }

    override fun confirmDelete() {
        val dialogBinding = ConfirmDeleteLayoutBinding.inflate(layoutInflater)
        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)

        dialog.setTitle("Confirm Delete")
        dialog.setMessage("do you want to delete?")
        dialog.show()

        dialogBinding.buttonsLayout.addButton("Delete") {
            mStoragePresenter.delete()
            dialog.cancel()
        }

        dialogBinding.buttonsLayout.addButton("Cancel") {
            dialog.cancel()
        }
    }

    override fun showDetails(name: String, lastModified: String, size: String, path: String) {
        val dialogBinding = FileDetailsLayoutBinding.inflate(layoutInflater)
        dialogBinding.tvDetailsName.text = name
        dialogBinding.tvDetailsLastModified.text = lastModified
        dialogBinding.tvDetailsSize.text = size
        dialogBinding.tvDetailsPath.text = path

        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)
        dialog.show()
        dialogBinding.buttonsLayout.addButton("Ok") { dialog.cancel() }
    }

    override fun showDetails(name: String, contains: String, lastModified: String, size: String, path: String) {
        val dialogBinding = FolderDetailsLayoutBinding.inflate(layoutInflater)
        dialogBinding.tvDetailsName.text = name
        dialogBinding.tvDetailsContain.text = contains
        dialogBinding.tvDetailsLastModified.text = lastModified
        dialogBinding.tvDetailsSize.text = size
        dialogBinding.tvDetailsPath.text = path

        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)
        dialog.show()
        dialogBinding.buttonsLayout.addButton("Ok") { dialog.cancel() }
    }

    override fun showDetails(contains: String, totalSize: String) {
        val dialogBinding = FilesDetailsLayoutBinding.inflate(layoutInflater)
        dialogBinding.tvDetailsContain.text = contains
        dialogBinding.tvDetailsTotalSize.text = totalSize

        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)
        dialog.show()
        dialogBinding.buttonsLayout.addButton("Ok") { dialog.cancel() }
    }

    override fun recreateView() {
        recreate()
    }

    override fun pickNewViewingSettings(dialog: AlertDialog, dialogBinding: PickViewSettingsLayoutBinding) {
        dialog.show()
        dialogBinding.buttonsLayout.addButton("Save") {
            val sortBy = when (dialogBinding.rgViewType.checkedRadioButtonId) {
                R.id.rbName -> {
                    SORTING_BY_NAME
                }
                R.id.rbSize -> {
                    SORTING_BY_SIZE
                }
                R.id.rbDate -> {
                    SORTING_BY_DATE
                }
                else -> DEFAULT_SORTING_ARGUMENT
            }
            val order = when (dialogBinding.rgViewOrder.checkedRadioButtonId) {
                R.id.rbAscending -> SORTING_ORDER_ASC
                R.id.rbDescending -> SORTING_ORDER_DEC
                else -> SORTING_ORDER_ASC
            }
            val showHiddenFiles = dialogBinding.btnShowHiddenFiles.isChecked
            val darkModeState = dialogBinding.btnDarkModeState.isChecked

            presenter.saveViewingSettings(sortBy, order, showHiddenFiles, darkModeState)
            dialog.cancel()
        }
    }

    override fun getScreenWidth() = binding.root.width

    override fun showPickLocation(fragment: PickPasteLocationDialogFragment) {
        fragment.show(supportFragmentManager, "Pick Paste Location")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (showMenu)
            menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onBreadcrumbsItemClicked(item: BreadcrumbsDataModel) {
//        navigating back to the fragment of the clicked breadcrumbs item
        mStoragePresenter.stopActionMode()
        supportFragmentManager.popBackStack(item.path, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

//      saving the top  folder path in the breadcrumbs so it can be recreated again after the
//      configuration change
        val topItemPath = breadcrumbsAdapter.list.last().path
        outState.putString(BREADCRUMBS_TOP_ITEM_PATH, topItemPath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val storageName = intent.getStringExtra(STORAGE_DISPLAY_NAME_EXTRA)!!
        val storagePath = intent.getStringExtra(STORAGE_PATH_EXTRA)!!
        val topPath = savedInstanceState.getString(BREADCRUMBS_TOP_ITEM_PATH)!!

        breadcrumbsAdapter.list = recreateBreadcrumbsFromPath(storageName, storagePath, topPath)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        check for sd card authorization
        if (resultCode == RESULT_OK && requestCode == PICKER_REQUEST_CODE) {
            val treeUri = data?.data!!
            if (mStoragePresenter.isValidTreeUri(treeUri)) {
//            sd card was selected successfully... save the uri
                mStoragePresenter.saveTreeUri(treeUri)

//                granting the permission to write to sd card
                grantUriPermission(
                    packageName,
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            Toast.makeText(this, "authorization successful !", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(this, "Authorization failed", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            permissions.forEachIndexed { index, s ->
                if (grantResults.isNotEmpty() && grantResults[index] == PackageManager.PERMISSION_GRANTED)
                    Log.d(TAG, " $s Granted!")
                else
                    Toast.makeText(this, "You need $s", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        // to update the screen with progress
        copyProgressReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val max = intent?.getIntExtra(MAX_PROGRESS_EXTRA, 0)!!
                val p = intent.getIntExtra(PROGRESS_EXTRA, 0)
                val leftDoneText = intent.getStringExtra(DONE_AND_LEFT_EXTRA)
                val name = intent.getStringExtra(CURRENT_COPY_ITEM_NAME_EXTRA)
                updateCopyProgress(p, max, leftDoneText!!, name!!)
            }
        }

        //to close the cancel the copying
        finishCopyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                mStoragePresenter.transferFinished(intent)
            }
        }

        val copyProgressFilter = IntentFilter(StorageView.ACTION_PROGRESS_UPDATE)
        val copyFinishFilter = IntentFilter(StorageView.ACTION_FINISH_TRANSFER)

        registerReceiver(copyProgressReceiver, copyProgressFilter)
        registerReceiver(finishCopyReceiver, copyFinishFilter)
    }

    override fun openProgressScreen(action: String) {
        val dialogBinding = ProgressDialogLayoutBinding.inflate(layoutInflater)
        val dialog = CustomDialog.makeDialog(this, dialogBinding.root)

        dialogBinding.tvCancel.setOnClickListener {
            // cancel the running work of the transfer
            // this dialog will get dismissed after this activity receive the finish intent
            sendBroadcast(Intent(CancelTransferBroadCast.ACTION))
        }

        dialog.setTitle(action)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        progressDialogBinding = dialogBinding
        progressDialog = dialog
    }

    override fun updateCopyProgress(p: Int, max: Int, leftDoneText: String, name: String) {
        val progress = String.format("%.2f", p.div(max.toFloat()).times(100)) + '%'
        progressDialogBinding?.let {
            it.apply {
                progressPercentage.text = progress
                tvItemsCount.text = leftDoneText
                tvCopyName.text = name
                copyProgressBar.max = max
                copyProgressBar.progress = p
            }
            progressDialog?.setView(it.root)
        }
    }

    override fun closeProgressScreen() {
        progressDialog?.cancel()
        progressDialogBinding = null
        progressDialog = null
    }

    override fun onStop() {
        unregisterReceiver(copyProgressReceiver)
        unregisterReceiver(finishCopyReceiver)
        super.onStop()
    }

    override fun onDestroy() {
        sendBroadcast(Intent(CancelTransferBroadCast.ACTION))
        super.onDestroy()
    }

    private fun openSearchFragment() {
        val storagePath = intent.getStringExtra(STORAGE_PATH_EXTRA)!!
        val searchFragment = SearchFragment.newInstance(storagePath)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, searchFragment)
            .setTransition(TRANSIT_FRAGMENT_FADE)
            .addToBackStack(SEARCH_FRAGMENT_TAG)
            .commit()
    }
}