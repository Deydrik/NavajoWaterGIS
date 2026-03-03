package com.example.navajowatergis.data

import com.example.navajowatergis.data.dao.WellDao
import com.example.navajowatergis.data.entities.WellAnalyteEntity
import com.example.navajowatergis.data.entities.WellEntity
import com.example.navajowatergis.data.entities.WellWithAnalytes
import kotlinx.coroutines.flow.Flow

class WellRepository(private val dao: WellDao) {

    // Flow of wells with analytes
    fun getAllWellsWithAnalytesFlow(): Flow<List<WellWithAnalytes>> = dao.getAllWellsWithAnalytesFlow()

    suspend fun getWellWithAnalytesById(id: Long): WellWithAnalytes? = dao.getWellWithAnalytesById(id)

    suspend fun getCount(): Int = dao.getCount()

    suspend fun clearAll() {
        dao.clearAnalytes()
        dao.clearWells()
    }

    /**
     * Insert an ImportedWell: insert WellEntity first to get the generated id,
     * then set wellId on analytes and insert them.
     */
    suspend fun insertImportedWells(imported: List<com.example.navajowatergis.data.ImportedWell>) {
        imported.forEach { iw ->
            val insertedId = dao.insertWell(iw.well)
            // set the wellId on analytes and insert
            if (iw.analytes.isNotEmpty()) {
                val analytesWithId = iw.analytes.map { a ->
                    a.copy(wellId = insertedId)
                }
                dao.insertAnalytes(analytesWithId)
            }
        }
    }
}
