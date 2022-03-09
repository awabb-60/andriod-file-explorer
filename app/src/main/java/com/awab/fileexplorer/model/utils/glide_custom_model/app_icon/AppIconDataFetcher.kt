package com.awab.fileexplorer.model.utils.glide_custom_model.app_icon

import android.content.Context
import android.graphics.drawable.Drawable
import com.awab.fileexplorer.model.data_models.AppIconModelData
import com.awab.fileexplorer.model.utils.getApkIcon
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

// this where the data will get loaded
class AppIconDataFetcher(val context: Context, private val model: AppIconModelData): DataFetcher<Drawable> {

    // the way the data get loaded
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {
        val icon = getApkIcon(context, model.path)
        if (icon != null)
            callback.onDataReady(icon)
        else
            callback.onLoadFailed(Exception("failed to load ${model.path} icon"))
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