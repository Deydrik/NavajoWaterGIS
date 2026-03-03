package com.example.navajowatergis.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single analyte measurement for a well.
 * wellId references WellEntity.id. Use CASCADE to delete analytes when a well is removed.
 */
@Entity(
    tableName = "well_analytes",
    foreignKeys = [
        ForeignKey(
            entity = WellEntity::class,
            parentColumns = ["id"],
            childColumns = ["wellId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wellId")]
)
data class WellAnalyteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var wellId: Long = 0L,         // set by repository when inserting analytes after inserting wells
    val analyteName: String,
    val concentration: Double? = null,
    val dateSampled: String? = null
)
