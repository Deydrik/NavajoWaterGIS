package com.example.navajowatergis.data

import com.example.navajowatergis.data.entities.WellAnalyteEntity
import com.example.navajowatergis.data.entities.WellEntity

/**
 * Helper structure returned by the DataImporter.
 * analytes may have wellId = 0; repository will set correct wellId when inserting.
 */
data class ImportedWell(
    val well: WellEntity,
    val analytes: List<WellAnalyteEntity> = emptyList()
)
