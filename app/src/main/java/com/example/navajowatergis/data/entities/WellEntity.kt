package com.example.navajowatergis.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wells")
data class WellEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val analyte: String? = null,
    val concentration: Double? = null,
    val dateSampled: String? = null
)
