package com.example.navajowatergis.data.dao

import androidx.room.*
import com.example.navajowatergis.data.entities.WellAnalyteEntity
import com.example.navajowatergis.data.entities.WellEntity
import com.example.navajowatergis.data.entities.WellWithAnalytes
import kotlinx.coroutines.flow.Flow

@Dao
interface WellDao {

    // Wells table operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWell(well: WellEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWells(wells: List<WellEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM wells")
    suspend fun getCount(): Int

    @Query("DELETE FROM wells")
    suspend fun clearWells()

    @Query("SELECT * FROM wells WHERE id = :id")
    suspend fun getWellById(id: Long): WellEntity?

    // Analytes table operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyte(analyte: WellAnalyteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytes(list: List<WellAnalyteEntity>): List<Long>

    @Query("DELETE FROM well_analytes")
    suspend fun clearAnalytes()

    // Relationship queries
    @Transaction
    @Query("SELECT * FROM wells ORDER BY name ASC")
    fun getAllWellsWithAnalytesFlow(): Flow<List<WellWithAnalytes>>

    @Transaction
    @Query("SELECT * FROM wells WHERE id = :id")
    suspend fun getWellWithAnalytesById(id: Long): WellWithAnalytes?
}
