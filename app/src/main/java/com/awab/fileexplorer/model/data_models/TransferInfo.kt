package com.awab.fileexplorer.model.data_models

import android.os.Parcel
import android.os.Parcelable
import com.awab.fileexplorer.model.types.TransferAction

/**
 * this class hold information about the transfer action had occurred
 */
data class TransferInfo (
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

    companion object CREATOR : Parcelable.Creator<TransferInfo> {
        override fun createFromParcel(parcel: Parcel): TransferInfo {
            return TransferInfo(parcel)
        }

        override fun newArray(size: Int): Array<TransferInfo?> {
            return arrayOfNulls(size)
        }
    }
}