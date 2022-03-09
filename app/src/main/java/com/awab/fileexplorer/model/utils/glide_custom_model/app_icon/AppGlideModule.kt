package com.awab.fileexplorer.model.utils.glide_custom_model.app_icon

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.model.data_models.AlbumCoverModelData
import com.awab.fileexplorer.model.data_models.AppIconModelData
import com.awab.fileexplorer.model.utils.glide_custom_model.audio_album_conver.AlbumCoverModelLoaderFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class AppIconGlideApp: AppGlideModule(){
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            AppIconModelData::class.java, Drawable::class.java,
            ApplicationIconModelLoaderFactory(context)
        )
        registry.prepend(
            AlbumCoverModelData::class.java, Drawable::class.java,
            AlbumCoverModelLoaderFactory(context)
        )
    }
}