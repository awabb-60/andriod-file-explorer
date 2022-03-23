package com.awab.fileexplorer.view.custom_views

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awab.fileexplorer.databinding.PickPasteLoacationDialogFragmnetBinding
import com.awab.fileexplorer.presenter.contract.StoragePresenterContract
import com.awab.fileexplorer.utils.DEFAULT_SORTING_ARGUMENT
import com.awab.fileexplorer.utils.DEFAULT_SORTING_ORDER
import com.awab.fileexplorer.utils.adapters.PastLocationFilesAdapter
import com.awab.fileexplorer.utils.data.data_models.FileDataModel
import com.awab.fileexplorer.utils.data.data_models.StorageDataModel
import com.awab.fileexplorer.utils.data.data_models.Tap
import com.awab.fileexplorer.utils.data.types.TransferAction
import com.awab.fileexplorer.utils.makeFileModels
import com.awab.fileexplorer.utils.makeFilesList
import com.awab.fileexplorer.view.contract.StorageView
import java.io.File

class PickPasteLocationDialogFragment : DialogFragment(),
    PastLocationFilesAdapter.LocationFilesListener {

    private lateinit var mStoragePresenter: StoragePresenterContract

    lateinit var storagesList: Array<StorageDataModel>
    lateinit var action: TransferAction

    private lateinit var filesAdapter: PastLocationFilesAdapter

    private val locationStack = mutableListOf<FileDataModel>()

    private var _binding: PickPasteLoacationDialogFragmnetBinding? = null
    val binding: PickPasteLoacationDialogFragmnetBinding
        get() = _binding!!

    private var fragmentWidth = 100

    private var currentStorage = storagesList[0]

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // catching the main storage presenter
        if (context is StorageView) {
            mStoragePresenter = context.presenter
            fragmentWidth = (context.getScreenWidth() * 0.9).toInt()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PickPasteLoacationDialogFragmnetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.layoutParams.width = fragmentWidth

        val taps = storagesList.map {
            Tap(it.name) {
                storageChanged(it)
            }
        }

        taps.forEach { binding.storagesTaps.addTap(it) }

        // this is the adapter for that display the files
        filesAdapter = PastLocationFilesAdapter(requireContext(), listOf(), this)
        binding.rvLocations.adapter = filesAdapter
        binding.rvLocations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLocations.setHasFixedSize(true)

        // dismissing the dialog
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // send the paste location to the storage presenter
        binding.btnPaste.setOnClickListener {
            val currentLocation =
                locationStack.last().path

            // start the transfer
            mStoragePresenter.transfer(currentLocation, currentStorage, action)
            dismiss()
        }

        // catching the back button clicks
        // to navigate back in the files list or to dismiss the dialog when reach the end
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                // removing the current location
                locationStack.removeAt(locationStack.indices.last())
                // if that was the last location in the stack
                if (locationStack.isEmpty()) {
                    dismiss()
                    return@setOnKeyListener true
                }
                // loading the previous location files
                val prevFiles = makeFilesList(
                    File(locationStack.last().path),
                    DEFAULT_SORTING_ARGUMENT,
                    DEFAULT_SORTING_ORDER,
                    true
                )
                filesAdapter.updateList(prevFiles)
                true
            } else
                false
        }
        binding.storagesTaps.selectTap(0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun storageChanged(storage: StorageDataModel) {
        locationStack.clear()
        val f = makeFileModels(listOf(File(storage.path)))
        locationStack.add(f[0])
        val files = makeFilesList(File(storage.path), DEFAULT_SORTING_ARGUMENT, DEFAULT_SORTING_ORDER, true)
        filesAdapter.updateList(files)
        currentStorage = storage
    }

    override fun onFileClick(file: FileDataModel) {
        // adding the parent file to the stack
        locationStack.add(file)

        // the new inner files
        val files = makeFilesList(File(file.path), DEFAULT_SORTING_ARGUMENT, DEFAULT_SORTING_ORDER, true)
        filesAdapter.updateList(files)
    }

    companion object {
        fun newInstance(
            storages: Array<StorageDataModel>,
            transferAction: TransferAction
        ): PickPasteLocationDialogFragment {
            return PickPasteLocationDialogFragment().apply {
                storagesList = storages
                action = transferAction
            }
        }
    }
}