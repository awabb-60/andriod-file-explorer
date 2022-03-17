package com.awab.fileexplorer.utils

import android.Manifest
import android.provider.MediaStore

//const val READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
const val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE

const val PERMISSION_REQUEST_CODE = 1
val  INTERNAL_STORAGE_REQUIRED_PERMISSIONS = arrayOf(WRITE_EXTERNAL_STORAGE_PERMISSION)


/**
 * the text that will show as the name of the storage item
 */
const val INTERNAL_STORAGE_DISPLAY_NAME = "Phone"
const val EXTERNAL_SDCARD_DISPLAY_NAME = "Sd Card"

/**
 * the storage path to start with
 */
const val STORAGE_PATH_EXTRA = "STORAGE_PATH_EXTRA"
const val STORAGE_DISPLAY_NAME_EXTRA = "STORAGE_NAME_EXTRA"

/**
 * the folder path to navigate to
 */
const val LOCATE_FOLDER_PATH_EXTRA = "NAVIGATE_TO_FOLDER_PATH_EXTRA"

const val PICKER_REQUEST_CODE = 10


const val STORAGES_LIST_EXTRA = "STORAGES_LIST_EXTRA"
const val STORAGE_TYPE_EXTRA = "STORAGE_TYPE_EXTRA"

const val SD_CARD_TREE_URI_SP = "SD_CARD_TREE_URI_SP"
const val TREE_URI_ = "TREE_URI_"

const val SEARCH_STORAGE_PATH_ARGS = "SEARCH_STORAGE_PATH_ARGS"
const val SEARCH_FRAGMENT_TAG = "SEARCH_FRAGMENT_TAG"

const val FILE_PATH_ARGS = "FILE_PATH_ARGS"

const val BREADCRUMBS_TOP_ITEM_PATH = "BREADCRUMBS_TOP_ITEM_PATH"

/**
 * the shared preferences name
 */
const val VIEW_SETTINGS_SHARED_PREFERENCES = "VIEW_TYPE_SHARED_PREFERENCES"

const val SHARED_PREFERENCES_SORTING_BY = "SHARED_PREFERENCES_SORTING_TYPE"
const val SHARED_PREFERENCES_SORTING_ORDER = "SHARED_PREFERENCES_SORTING_ORDER"
const val SHARED_PREFERENCES_SHOW_HIDDEN_FILES = "SHARED_PREFERENCES_SHOW_HIDDEN_FILES"

// the sorting arguments
const val SORTING_BY_NAME = "SORTING_BY_NAME"
const val SORTING_BY_SIZE = "SORTING_BY_SIZE"
const val SORTING_BY_DATE = "SORTING_BY_DATE"

const val DEFAULT_SORTING_ARGUMENT = SORTING_BY_NAME

// the sorting order arguments
const val SORTING_ORDER_ASC = "SORTING_ORDER_ASC"
const val SORTING_ORDER_DEC = "SORTING_TYPE_DEC"

const val DEFAULT_SORTING_ORDER = SORTING_ORDER_ASC
const val DEFAULT_SHOW_HIDDEN_FILES = false

const val DATE_FORMAT_PATTERN = "E dd-MMM-y  hh:mm a"

/**
 * the type of transfer that will happened
 */
const val TRANSFER_ACTION_EXTRA = "COPY_TYPE_EXTRA"

/**
 * the paths of the files that will get transferred
 */
const val TRANSFER_FILES_PATHS_EXTRA = "TRANSFER_FILES_PATHS_EXTRA"

/**
 * the location where the files will be transfer to
 */
const val PASTE_LOCATION_PATH_EXTRA = "PASTE_LOCATION_PATH_EXTRA"

/**
 * the location storage model where the files will be transfer to
 */
const val PASTE_LOCATION_STORAGE_MODEL_EXTRA = "PASTE_LOCATION_STORAGE_MODEL_EXTRA"

/**
 * the tree uri that will get used to write to the sd card
 */
const val TREE_URI_FOR_TRANSFER_EXTRA = "TREE_URI_FOR_TRANSFER_EXTRA"

const val EXTERNAL_STORAGE_PATH_EXTRA = "EXTERNAL_STORAGE_PATH_EXTRA"


/**
 * the gap between progress update
 */
const val progressUpdateAfter = 2000

/**
 * the name of the file that getting transferred
 */
const val CURRENT_COPY_ITEM_NAME_EXTRA = "CURRENT_COPY_ITEM_NAME_EXTRA"

/**
 * the number of files that has been transferred to the total number of files
 */
const val DONE_AND_LEFT_EXTRA = "DONE_AND_LEFT_EXTRA"

/**
 * the number of bytes that has been copied
 */
const val PROGRESS_EXTRA = "PROGRESS_EXTRA"

/**
 * the total number of bytes to copy
 */
const val MAX_PROGRESS_EXTRA = "MAX_PROGRESS_EXTRA"

/**
 * to tell if all the files was moved successfully
 */
const val TRANSFER_INFO_EXTRA = "MOVE_SUCCESSFUL_EXTRA"

/**
 * intent that mark the end of the transfer
 */
const val FINISH_COPY_INTENT = "FINISH_COPY_INTENT"

/**
 * this the category of media to be shown in the media activity
 */
const val MEDIA_CATEGORY_EXTRA = "MEDIA_CATEGORY_EXTRA"

val PROJECTION = arrayOf(
    MediaStore.MediaColumns.DATA,
    MediaStore.MediaColumns.DISPLAY_NAME,
    MediaStore.MediaColumns.SIZE,
    MediaStore.MediaColumns.MIME_TYPE
)