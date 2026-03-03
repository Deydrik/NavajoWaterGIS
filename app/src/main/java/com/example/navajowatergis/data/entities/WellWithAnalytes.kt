package com.example.navajowatergis.data.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relationship: a WellEntity with its associated analytes
 */
data class WellWithAnalytes(
    @Embedded val well: WellEntity,
    @Relation(parentColumn = "id", entityColumn = "wellId")
    val analytes: List<WellAnalyteEntity>
)
