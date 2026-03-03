package com.example.navajowatergis.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.navajowatergis.data.WellDatabase
import com.example.navajowatergis.data.WellRepository
import com.example.navajowatergis.data.ImportedWell
import com.example.navajowatergis.data.entities.WellAnalyteEntity
import com.example.navajowatergis.data.entities.WellWithAnalytes
import com.example.navajowatergis.utils.DataImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * ViewModel exposing wells with analytes.
 * Applies search, sort modes, and supports nearest sorting when user location is set.
 */
class WellViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = WellDatabase.getInstance(application).wellDao()
    private val repository = WellRepository(dao)

    private val _searchQuery = MutableStateFlow("")
    fun setSearchQuery(q: String) { _searchQuery.value = q }

    enum class SortMode { NAME_ASC, ANALYTE_ASC, NEAREST }
    private val _sortMode = MutableStateFlow(SortMode.NAME_ASC)
    fun setSortMode(mode: SortMode) { _sortMode.value = mode }

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    fun setUserLocation(lat: Double, lng: Double) { _userLocation.value = Pair(lat, lng) }

    // base flow
    private val baseFlow = repository.getAllWellsWithAnalytesFlow()

    // combined flow applying search and sort, emits List<WellWithAnalytes>
    private val combined = combine(baseFlow, _searchQuery, _sortMode, _userLocation) { wellsWithAnalytes, query, sortMode, userLoc ->
        var list = if (query.isBlank()) wellsWithAnalytes else wellsWithAnalytes.filter { gwa ->
            val nameMatch = gwa.well.name.contains(query, ignoreCase = true)
            val analyteMatch = gwa.analytes.any { it.analyteName.contains(query, ignoreCase = true) }
            nameMatch || analyteMatch
        }

        when (sortMode) {
            SortMode.NAME_ASC -> list = list.sortedBy { it.well.name.lowercase() }
            SortMode.ANALYTE_ASC -> list = list.sortedWith(compareBy { it.analytes.joinToString(", ") { a -> a.analyteName.lowercase() } })
            SortMode.NEAREST -> {
                val loc = userLoc
                if (loc != null) {
                    val (ulat, ulng) = loc
                    list = list.sortedBy { distanceKmBetween(ulat, ulng, it.well.latitude, it.well.longitude) }
                }
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wellsLive = combined.asLiveData()

    /**
     * Preload from assets into DB if empty.
     * Uses DataImporter.importFromAsset which returns ImportedWell objects.
     */
    fun preloadSampleDataIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.getCount()
            if (count == 0) {
                val assets = getApplication<Application>().assets.list("")?.toList() ?: emptyList()
                val name = when {
                    "sample_wells.json" in assets -> "sample_wells.json"
                    "sample_wells.csv" in assets -> "sample_wells.csv"
                    else -> null
                }
                name?.let {
                    val imported: List<ImportedWell> = DataImporter.importFromAsset(getApplication(), it)
                    repository.insertImportedWells(imported)
                }
            }
        }
    }

    // nearest helper
    fun findNearest(wells: List<WellWithAnalytes>, lat: Double, lng: Double) =
        wells.minByOrNull { distanceKmBetween(lat, lng, it.well.latitude, it.well.longitude) }

    private fun distanceKmBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
