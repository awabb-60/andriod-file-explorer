package com.awab.fileexplorer.model.types

import android.os.Parcel
import android.os.Parcelable

/**
 * the way the files will get transferred
 */
enum class TransferAction : Parcelable {
    COPY,
    MOVE;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(this.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TransferAction> {
        override fun createFromParcel(parcel: Parcel): TransferAction {
            return valueOf(parcel)
        }

        override fun newArray(size: Int): Array<TransferAction?> {
            return arrayOfNulls(size)
        }

        private fun valueOf(parcel: Parcel):TransferAction{
            return when(parcel.readString()){
                COPY.name->COPY
                MOVE.name->MOVE
                else->COPY
            }
        }
    }
}