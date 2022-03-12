package com.awab.fileexplorer.utils.glide_custom_model.app_icon

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.utils.data.data_models.AppIconDataModel
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

class ApplicationIconModelLoaderFactory(val context: Context): ModelLoaderFactory<AppIconDataModel, Drawable> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AppIconDataModel, Drawable> {
        return ApplicationIconModelLoader(context)
    }

    override fun teardown() {
    }
}