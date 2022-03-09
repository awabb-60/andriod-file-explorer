package com.awab.fileexplorer.model.utils.glide_custom_model.app_icon

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.model.data_models.AppIconModelData
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

class ApplicationIconModelLoaderFactory(val context: Context): ModelLoaderFactory<AppIconModelData, Drawable> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AppIconModelData, Drawable> {
        return ApplicationIconModelLoader(context)
    }

    override fun teardown() {
    }
}