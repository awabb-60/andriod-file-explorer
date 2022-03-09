package com.awab.fileexplorer.model.utils.glide_custom_model.audio_album_conver

import android.content.ContentUris
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import com.awab.fileexplorer.model.data_models.AlbumCoverModelData
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

// this where the data will get loaded
class AlbumCoverDataFetcher(val context: Context, private val model: AlbumCoverModelData) : DataFetcher<Drawable> {

    // the way the data get loaded
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {

        // getting a query that contain only the data match the model name
        val query = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ALBUM_ID),
            "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?",
            arrayOf("%${model.name}%"), null
        )

        // the album cover drawable
        var coverDrawable: Drawable? = null
        query?.let { cursor ->
            cursor.moveToFirst()
            val albumColumnId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            val albumId = cursor.getLong(albumColumnId)

            val artWorkUri = Uri.parse("content://media/external/audio/albumart")

            val coverUri = ContentUris.withAppendedId(artWorkUri, albumId)

            // the data must only has one name
            if (!cursor.moveToNext()) {
                val stream = context.contentResolver.openInputStream(coverUri)
                coverDrawable = Drawable.createFromStream(stream, coverUri.toString())
            }
        }

        query?.close()
        if (coverDrawable != null)
            callback.onDataReady(coverDrawable)
        else
            callback.onLoadFailed(Exception("album cover not found"))
    }

    override fun cleanup() {
    }

    // to cancel long running loading
    override fun cancel() {
    }

    override fun getDataClass(): Class<Drawable> {
        return Drawable::class.java
    }

    // the source of the data... local data base or HTTP
    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}