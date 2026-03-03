package com.example.navajowatergis.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.navajowatergis.R
import com.example.navajowatergis.data.entities.WellAnalyteEntity

class WellAnalyteAdapter : ListAdapter<WellAnalyteEntity, WellAnalyteAdapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<WellAnalyteEntity>() {
            override fun areItemsTheSame(oldItem: WellAnalyteEntity, newItem: WellAnalyteEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: WellAnalyteEntity, newItem: WellAnalyteEntity) =
                oldItem == newItem
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val analyteName: TextView = view.findViewById(R.id.analyte_name)
        val concentration: TextView = view.findViewById(R.id.analyte_concentration)
        val date: TextView = view.findViewById(R.id.analyte_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_analyte, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val a = getItem(position)
        holder.analyteName.text = a.analyteName
        holder.concentration.text = a.concentration?.toString() ?: "N/A"
        holder.date.text = a.dateSampled ?: "Unknown"
    }
}
