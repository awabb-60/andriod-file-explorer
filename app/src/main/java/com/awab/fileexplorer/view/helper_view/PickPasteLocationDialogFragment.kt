package com.awab.fileexplorer.view.helper_view

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.adapters.PastLocationFilesAdapter
import com.awab.fileexplorer.adapters.PastLocationStoragesAdapter
import com.awab.fileexplorer.databinding.PickPasteLoacationDialogFragmnetBinding
import com.awab.fileexplorer.model.data_models.FileModel
import com.awab.fileexplorer.model.data_models.StorageModel
import com.awab.fileexplorer.model.types.StorageType
import com.awab.fileexplorer.model.utils.DEFAULT_SORTING_ARGUMENT
import com.awab.fileexplorer.model.utils.DEFAULT_SORTING_ORDER
import com.awab.fileexplorer.model.utils.makeFileModels
import com.awab.fileexplorer.model.utils.makeFilesList
import java.io.File

class PickPasteLocationDialogFragment : DialogFragment(),
    PastLocationStoragesAdapter.LocationStoragesListener,
    PastLocationFilesAdapter.LocationFilesListener {

    private val TAG = "PickPasteLocationDialog"
    lateinit var storagesList: ArrayList<String>
    private lateinit var filesAdapter: PastLocationFilesAdapter
    private lateinit var storagesAdapter: PastLocationStoragesAdapter

    private val locationStack = mutableListOf<FileModel>()

    private var _binding: PickPasteLoacationDialogFragmnetBinding? = null
    val binding: PickPasteLoacationDialogFragmnetBinding
        get() = _binding!!

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

        val l = ArrayList<StorageModel>()
        storagesList.forEach {
            l.add(StorageModel(File(it).name, "90", 1000, 1000, it, StorageType.INTERNAL))
        }
        storagesAdapter = PastLocationStoragesAdapter(l.toArray(arrayOf<StorageModel>()), this)
        binding.storageListView.adapter = storagesAdapter
        binding.storageListView.layoutManager = GridLayoutManager(
            requireContext(), l.size,
            RecyclerView.VERTICAL, false
        )
        binding.storageListView.setHasFixedSize(true)

        filesAdapter = PastLocationFilesAdapter(requireContext(), listOf(), this)
        binding.rvLocations.adapter = filesAdapter
        binding.rvLocations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLocations.setHasFixedSize(true)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnPaste.setOnClickListener {
            val currentLocation =
                locationStack.last().path
            Toast.makeText(context, currentLocation, Toast.LENGTH_SHORT).show()
        }

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
        storageChanged(storagesAdapter.storages[0])
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun storageChanged(storage: StorageModel) {
        locationStack.clear()
        val f = makeFileModels(listOf(File(storage.path)))
        locationStack.add(f[0])
        val files = makeFilesList(File(storage.path), DEFAULT_SORTING_ARGUMENT, DEFAULT_SORTING_ORDER, true)
        filesAdapter.updateList(files)
    }

    override fun onFileClick(file: FileModel) {
        // adding the parent file to the stack
        locationStack.add(file)

        // the new inner files
        val files = makeFilesList(File(file.path), DEFAULT_SORTING_ARGUMENT, DEFAULT_SORTING_ORDER, true)
        filesAdapter.updateList(files)
    }

    companion object {
        fun newInstance(storages: ArrayList<String>): PickPasteLocationDialogFragment {
            return PickPasteLocationDialogFragment().apply {
                storagesList = storages
            }
        }
    }
}