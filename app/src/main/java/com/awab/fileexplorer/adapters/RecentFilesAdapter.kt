package com.awab.fileexplorer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.awab.fileexplorer.R
import kotlin.math.min


class RecentFilesAdapter : RecyclerView.Adapter<RecentFilesAdapter.ViewHolder>() {
    private var itemsList = listOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


        val cardWidth = parent.width / 3
        val cardHeight = parent.height / 5
        val cardSideLength = min(cardWidth,cardHeight)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_file_item, parent, false)
        val layoutParams = view.findViewById<CardView>(R.id.recentFileCard).layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.width =  cardSideLength
        layoutParams.height = cardSideLength
        layoutParams.setMargins(1,1,1,1)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemsList[position]
        holder.name.text = currentItem
        holder.image.setImageResource(R.drawable.ic_default_file)
    }

    override fun getItemCount(): Int = itemsList.size

    fun setList(list: List<String>) {
        itemsList = list
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.ivRecentFileImage)
        var name: TextView = itemView.findViewById(R.id.tvRecentFilesName)
    }

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
    }
}