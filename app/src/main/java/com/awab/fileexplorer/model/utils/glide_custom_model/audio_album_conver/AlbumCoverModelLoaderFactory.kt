package com.awab.fileexplorer.model.utils.glide_custom_model.audio_album_conver

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.model.data_models.AlbumCoverModelData
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

class AlbumCoverModelLoaderFactory(val context: Context) : ModelLoaderFactory<AlbumCoverModelData, Drawable> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AlbumCoverModelData, Drawable> {
        return AlbumCoverModelLoader(context)
    }

    override fun teardown() {
    }
}