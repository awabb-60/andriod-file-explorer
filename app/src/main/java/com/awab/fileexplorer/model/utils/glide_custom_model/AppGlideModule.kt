package com.awab.fileexplorer.model.utils.glide_custom_model

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.model.data_models.AppIconModelData
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
    }
}