package com.awab.fileexplorer.utils.data.data_models

import android.os.Parcel
import android.os.Parcelable
import com.awab.fileexplorer.utils.data.types.TransferAction

/**
 * this class hold information about the transfer action had occurred
 */
data class TransferInfoDataModel (
    val wasSuccessful: Boolean = false,
    val messages: ArrayList<String>,
    val action: TransferAction
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.createStringArrayList() as ArrayList<String>,
        TransferAction.valueOf(parcel.readString()!!)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (wasSuccessful) 1 else 0)
        parcel.writeStringList(messages)
        parcel.writeString(action.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TransferInfoDataModel> {
        override fun createFromParcel(parcel: Parcel): TransferInfoDataModel {
            return TransferInfoDataModel(parcel)
        }

        override fun newArray(size: Int): Array<TransferInfoDataModel?> {
            return arrayOfNulls(size)
        }
    }
}