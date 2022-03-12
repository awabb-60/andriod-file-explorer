package com.awab.fileexplorer.utils.glide_custom_model.audio_album_conver

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.utils.data.data_models.AlbumCoverDataModel
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

class AlbumCoverModelLoaderFactory(val context: Context) : ModelLoaderFactory<AlbumCoverDataModel, Drawable> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AlbumCoverDataModel, Drawable> {
        return AlbumCoverModelLoader(context)
    }

    override fun teardown() {
    }
}