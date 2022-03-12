package com.awab.fileexplorer.utils.data.types

/**
 * the storage type
 */
enum class StorageType {
    INTERNAL,
    SDCARD,
    USB_MOUNTED;


    companion object {
        fun valueOf(name: String): StorageType? {
            return when (name) {
                INTERNAL.name -> INTERNAL
                SDCARD.name -> SDCARD
                USB_MOUNTED.name -> USB_MOUNTED
                else -> null
            }
        }
    }
}