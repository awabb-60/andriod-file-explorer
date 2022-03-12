package com.awab.fileexplorer.utils.glide_custom_model

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.utils.data.data_models.AlbumCoverDataModel
import com.awab.fileexplorer.utils.data.data_models.AppIconDataModel
import com.awab.fileexplorer.utils.glide_custom_model.app_icon.ApplicationIconModelLoaderFactory
import com.awab.fileexplorer.utils.glide_custom_model.audio_album_conver.AlbumCoverModelLoaderFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * registering the glide custom model
 */
@GlideModule
class AppIconGlideApp: AppGlideModule(){
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            AppIconDataModel::class.java, Drawable::class.java,
            ApplicationIconModelLoaderFactory(context)
        )
        registry.prepend(
            AlbumCoverDataModel::class.java, Drawable::class.java,
            AlbumCoverModelLoaderFactory(context)
        )
    }
}