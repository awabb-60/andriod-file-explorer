package com.awab.fileexplorer.utils.data.data_models

import android.os.Parcel
import android.os.Parcelable
import com.awab.fileexplorer.utils.data.types.StorageType

/**
 * a data class to represent a storage
 */
data class StorageDataModel(
    val name:String,
    val size:String,
    val totalSizeBytes: Long,
    val freeSizeBytes: Long,
    val path:String,
    val storageType: StorageType
                         ):Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!,
        StorageType.valueOf(parcel.readString()!!)
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(size)
        dest?.writeLong(totalSizeBytes)
        dest?.writeLong(freeSizeBytes)
        dest?.writeString(path)
        dest?.writeString(storageType.name)
    }

    companion object CREATOR : Parcelable.Creator<StorageDataModel> {
        override fun createFromParcel(parcel: Parcel): StorageDataModel {
            return StorageDataModel(parcel)
        }

        override fun newArray(size: Int): Array<StorageDataModel?> {
            return arrayOfNulls(size)
        }
    }
}