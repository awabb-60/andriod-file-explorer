package com.awab.fileexplorer.view


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.*
import com.awab.fileexplorer.adapters.BreadcrumbsAdapter
import com.awab.fileexplorer.presenter.*
import com.awab.fileexplorer.databinding.*
import com.awab.fileexplorer.model.data_models.BreadcrumbsModel
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.types.StorageType
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.model.utils.*
import com.awab.fileexplorer.model.utils.listeners.BreadcrumbsListener
import com.awab.fileexplorer.view.action_mode_callbacks.FilesActionModeCallBack
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File

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

    override fun onCreate(savedInstanceState: Bundle?) {
        createController()
        super.onCreate(savedInstanceState)
        binding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.selectToolBar)
        supportActionBar?.title = ""

        binding.selectToolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.miCreateFolder -> createNewFolder()
                R.id.miView -> pickViewType()
                R.id.miSearch -> openSearchFragment()
            }
            Log.d(TAG, "menu item clicked: toolBar listener")
            true
        }

        breadcrumbsAdapter = BreadcrumbsAdapter().apply {
            setListener(this@StorageActivity as BreadcrumbsListener)
        }
        binding.rvBreadcrumbs.adapter = breadcrumbsAdapter
        binding.rvBreadcrumbs.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        //  to close this activity when back pressed
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0)
                finish()
        }

//        back arrow in the tool bar
        binding.ivBackButton.setOnClickListener {
            onBackPressed()
        }

        // opening the storage folder
        if (savedInstanceState == null) {
            val storageName = intent.getStringExtra(STORAGE_DISPLAY_NAME_EXTRA)!!
            val storagePath = intent.getStringExtra(STORAGE_PATH_EXTRA)!!
            navigateToFolder(storageName, storagePath)
        }

    }

    private fun createController() {
        val storagePath = intent.getStringExtra(STORAGE_PATH_EXTRA)!!
        val rawStorageName = File(storagePath).name
//        getting the controller type... sd controller has more work
        val type = intent.getSerializableExtra(STORAGE_TYPE_EXTRA)!!
        if (type is StorageType) {
            when (type) {
                StorageType.INTERNAL -> {
                    mStoragePresenter = InternalStoragePresenter(this, rawStorageName, storagePath)
                }
                StorageType.SDCARD -> {
                    mStoragePresenter = SdCardPresenter(this, rawStorageName, storagePath)
                }
            }
        }
    }

    override val presenter: StoragePresenterContract
        get() = mStoragePresenter

    override fun context(): Context {
        return this
    }

    override fun openFile(intent: Intent) {
        startActivity(intent)
    }

    override fun navigateToFolder(name: String, path: String) {
        val fileFragment = FilesFragment.newInstance(path, mStoragePresenter)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fileFragment)
            .setTransition(TRANSIT_FRAGMENT_FADE)
            .addToBackStack(path)
            .commit()
        breadcrumbsAdapter.add(BreadcrumbsModel(name, path))
//        to scroll the adapter to the last breadcrumbs item
        binding.rvBreadcrumbs.smoothScrollToPosition(breadcrumbsAdapter.list.count() - 1)
    }

    override fun onFileClickFromSerach(file: FileModel) {
    }

    override fun removeBreadcrumb() {
        breadcrumbsAdapter.removeLast()
    }

    override fun openAuthorizationPicker(intent: Intent, requestCode: Int) {
        Toast.makeText(this, "select the sd card", Toast.LENGTH_LONG).show()
        startActivityForResult(intent, requestCode)
    }

    override fun startActionMode() {
        actionMode = startSupportActionMode(FilesActionModeCallBack(mStoragePresenter))
    }

    override fun updateActionMode() {
        actionMode?.invalidate()
    }

    override fun stopActionMode() {
        actionMode?.finish()
    }

    override fun confirmDelete() {
        if (!mStoragePresenter.isAuthorized()) {
            mStoragePresenter.requestPermission()
            return
        }

        val dialogBinding = ConfirmDeleteLayoutBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("do you want to delete?")
            .setView(dialogBinding.root)
            .create()

        dialog.show()

        dialogBinding.tvDelete.setOnClickListener {
            mStoragePresenter.delete()
            dialog.cancel()
        }

        dialogBinding.tvCancel.setOnClickListener {
            dialog.cancel()
        }
    }

    override fun showRenameDialog(path: String, currentName: String) {
        if (!mStoragePresenter.isAuthorized()) {
            mStoragePresenter.requestPermission()
            return
        }

        val binding = NamingFileLayoutBinding.inflate(layoutInflater, null, false)
        binding.etNameFile.setText(currentName)

        val dialog = AlertDialog.Builder(this).setTitle("Rename File")
            .setView(binding.root)
            .create()

        dialog.show()

//        focusing on the edit text
        binding.etNameFile.requestFocus()
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etNameFile, InputMethodManager.SHOW_IMPLICIT)

        binding.tvSave.setOnClickListener {

            if (binding.etNameFile.text.isNotBlank()) {
                val name = binding.etNameFile.text.toString().trim()
                mStoragePresenter.rename(path, name)
            } else
                Toast.makeText(this, "invalid name", Toast.LENGTH_SHORT).show()
            dialog.cancel()
        }
    }

    override fun showItemDetails(name: String, lastModified: String, size: String, path: String) {
        val dialogBinding = ItemDetailsLayoutBinding.inflate(layoutInflater)
        dialogBinding.tvDetailsName.text = name
        dialogBinding.tvDetailslastModified.text = lastModified
        dialogBinding.tvDetailsSize.text = size
        dialogBinding.tvDetailsPath.text = path

        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()
        dialog.show()
        dialogBinding.tvOk.setOnClickListener { dialog.cancel() }
    }

    override fun showItemsDetails(contains: String, totalSize: String) {
        val dialogBinding = ItemsDetailsLayoutBinding.inflate(layoutInflater)
        dialogBinding.tvDetailsContains.text = contains
        dialogBinding.tvDetailsTotalSize.text = totalSize

        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()
        dialog.show()
        dialogBinding.tvOk.setOnClickListener { dialog.cancel() }
    }

    override fun startCopyScreen() {
        if (!mStoragePresenter.isAuthorized()) {
            mStoragePresenter.requestPermission()
            return
        }

        val dialogBinding = ChoseLocationBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Choose Copy Location")
            .setView(dialogBinding.root).create()

        dialog.show()

        dialogBinding.tvCancel.setOnClickListener {
            dialog.cancel()
        }
        dialogBinding.tvCommit.setOnClickListener {

            when (dialogBinding.rgGroup.checkedRadioButtonId) {
                R.id.rbInternal -> mStoragePresenter.copy("I")
                R.id.rbSdCard -> mStoragePresenter.copy("S")
            }
            dialog.cancel()

        }

    }

    override fun startMoveScreen() {
        if (!mStoragePresenter.isAuthorized()) {
            mStoragePresenter.requestPermission()
            return
        }
        val dialogBinding = ChoseLocationBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Choose Move Location")
            .setView(dialogBinding.root).create()

        dialog.show()

        dialogBinding.tvCancel.setOnClickListener {
            dialog.cancel()
        }

        dialogBinding.tvCommit.setOnClickListener {
            when (dialogBinding.rgGroup.checkedRadioButtonId) {
                R.id.rbInternal -> mStoragePresenter.move("I")
                R.id.rbSdCard -> mStoragePresenter.move("S")
            }
            dialog.cancel()
        }
    }

    override fun openCopyProgress(action:String) {
        val dialogBinding = ProgressDialogLayoutBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvCancel.setOnClickListener {
            dialog.cancel()
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

    override fun stopCloseCopyScreen() {
        progressDialog?.cancel()
        progressDialogBinding = null
        progressDialog = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onBreadcrumbsItemClicked(item: BreadcrumbsModel) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val treeUri = data?.data!!

                if (mStoragePresenter.isValidTreeUri(treeUri)) {
//            sd card was selected successfully... save the uri
                    mStoragePresenter.saveTreeUri(treeUri)

//                granting the permission to write to sd card
                    grantUriPermission(
                        packageName,
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                Toast.makeText(this, "authorization successful !", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Authorization failed", Toast.LENGTH_SHORT).show()
            }
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
                mStoragePresenter.cancelCopy()
            }
        }

        val copyProgressFilter = IntentFilter(PROGRESS_INTENT)
        val copyFinishFilter = IntentFilter(FINISH_COPY_INTENT)

        registerReceiver(copyProgressReceiver, copyProgressFilter)
        registerReceiver(finishCopyReceiver, copyFinishFilter)
    }

    override fun onStop() {
        unregisterReceiver(copyProgressReceiver)
        unregisterReceiver(finishCopyReceiver)
        super.onStop()
    }

    private fun openSearchFragment() {
        val path = breadcrumbsAdapter.list.last().path
        val searchFragment = SearchFragment.newInstance(path)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, searchFragment)
            .addToBackStack(SEARCH_FRAGMENT_TAG)
            .commit()
    }

    private fun navigateToFolderFromSearch(name: String, path: String) {
//        removing the search fragment
        supportFragmentManager.popBackStack()
        navigateToFolder(name, path)
    }


    private fun createNewFolder() {
//        checking the authorization for the sdCard
        if (!mStoragePresenter.isAuthorized()) {
            mStoragePresenter.requestPermission()
            return
        }

        val dirPath = breadcrumbsAdapter.list.last().path
        val binding = NamingFileLayoutBinding.inflate(layoutInflater, null, false)
        val dialog = AlertDialog.Builder(this).setTitle("Create Folder")
            .setView(binding.root)
            .create()
        dialog.show()

        binding.etNameFile.requestFocus()
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etNameFile, 0)

        binding.tvSave.setOnClickListener {
            val etCreateFolderName = binding.etNameFile
            if (etCreateFolderName.text.isNotBlank()) {
                val name = etCreateFolderName.text.toString()
                mStoragePresenter.createFolder("$dirPath/$name")
            }
            dialog.cancel()
        }
    }

    private fun pickViewType() {
        val viewingData = loadViewingData()
        val binding = PickViewTypeLayoutBinding.inflate(layoutInflater, null, false)
//        val pickViewTypeView = layoutInflater.inflate(R.layout.pick_view_type_layout, null)

        val rgSortingType: RadioGroup = binding.rgViewType
        val rgSortingOrder: RadioGroup = binding.rgViewOrder
        when (viewingData[0]) {
            SORTING_TYPE_NAME -> rgSortingType.check(R.id.rbName)
            SORTING_TYPE_SIZE -> rgSortingType.check(R.id.rbSize)
            SORTING_TYPE_DATE -> rgSortingType.check(R.id.rbDate)
        }
        when (viewingData[1]) {
            SORTING_ORDER_ASC -> rgSortingOrder.check(R.id.rbAscending)
            SORTING_ORDER_DEC -> rgSortingOrder.check(R.id.rbDescending)
        }

        val dialog = AlertDialog.Builder(this).setView(binding.root).create()
        dialog.show()
        binding.tvSave.setOnClickListener {
            val type = when (rgSortingType.checkedRadioButtonId) {
                R.id.rbName -> {
                    SORTING_TYPE_NAME
                }
                R.id.rbSize -> {
                    SORTING_TYPE_SIZE
                }
                R.id.rbDate -> {
                    SORTING_TYPE_DATE
                }
                else -> "Noop"
            }
            val order = when (rgSortingOrder.checkedRadioButtonId) {
                R.id.rbAscending -> SORTING_ORDER_ASC
                R.id.rbDescending -> SORTING_ORDER_DEC
                else -> "noop"
            }
            saveViewingData(type, order)
            refreshTopFragment()
            dialog.cancel()
        }
    }

    private fun loadViewingData(): List<String> {
        val sp = getSharedPreferences(VIEW_TYPE_SHARED_PREFERENCES, MODE_PRIVATE)
        val savedType = sp.getString(SHARED_PREFERENCES_SORTING_TYPE, SORTING_TYPE_NAME)
        val savedOrder = sp.getString(SHARED_PREFERENCES_SORTING_ORDER, SORTING_ORDER_ASC)
        return listOf(savedType!!, savedOrder!!)
    }

    private fun saveViewingData(type: String, order: String) {
        val sharedPreferencesEditor = getSharedPreferences(VIEW_TYPE_SHARED_PREFERENCES, MODE_PRIVATE).edit()
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_TYPE, type)
        sharedPreferencesEditor.putString(SHARED_PREFERENCES_SORTING_ORDER, order)
        sharedPreferencesEditor.apply()
    }

    private fun refreshTopFragment() {
        val topFragment = supportFragmentManager.fragments.last()
        if (topFragment is FilesFragment) {
            topFragment.refreshList()
        }
    }

    private fun showAlertDialog(
        title: String?,
        message: String?,
        view: View?,
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setView(view)
            .show()

    }

    private fun BuildAlertDialog() = AlertDialog.Builder(this)
}