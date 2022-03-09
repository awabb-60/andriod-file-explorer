package com.awab.fileexplorer.model.utils.glide_custom_model.audio_album_conver

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.model.data_models.AlbumCoverModelData
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey

class AlbumCoverModelLoader(val context: Context) : ModelLoader<AlbumCoverModelData, Drawable> {

    // checks wither the data has been already cached
    override fun buildLoadData(
        model: AlbumCoverModelData,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<Drawable> {
        // the key that will get used to get the cache for your data
        val key = ObjectKey(model)
        return ModelLoader.LoadData(key, AlbumCoverDataFetcher(context, model))
    }

    //checks if the data is valid for this model
    override fun handles(model: AlbumCoverModelData): Boolean {
        return model.handleable
    }
}