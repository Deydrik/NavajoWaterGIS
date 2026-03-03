package com.example.navajowatergis.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.navajowatergis.R
import com.example.navajowatergis.data.entities.WellWithAnalytes

class WellListAdapter(private val onClick: (WellWithAnalytes) -> Unit) :
    ListAdapter<WellWithAnalytes, WellListAdapter.WellViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<WellWithAnalytes>() {
            override fun areItemsTheSame(oldItem: WellWithAnalytes, newItem: WellWithAnalytes): Boolean =
                oldItem.well.id == newItem.well.id

            override fun areContentsTheSame(oldItem: WellWithAnalytes, newItem: WellWithAnalytes): Boolean =
                oldItem == newItem
        }
    }

    inner class WellViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.nameText)
        private val analyteText: TextView = view.findViewById(R.id.analyteText)
        private val locationText: TextView = view.findViewById(R.id.locationText)

        fun bind(item: WellWithAnalytes) {
            nameText.text = item.well.name
            // join analyte names into comma-separated string
            val analytes = if (item.analytes.isEmpty()) {
                "No analytes"
            } else {
                item.analytes.joinToString(", ") { it.analyteName }
            }
            analyteText.text = analytes
            locationText.text = "Lat: ${item.well.latitude}, Lng: ${item.well.longitude}"

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WellViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_well, parent, false)
        return WellViewHolder(view)
    }

    override fun onBindViewHolder(holder: WellViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
