package com.example.navajowatergis.utils

import android.content.Context
import com.example.navajowatergis.data.ImportedWell
import com.example.navajowatergis.data.entities.WellAnalyteEntity
import com.example.navajowatergis.data.entities.WellEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Importer that returns ImportedWell objects: a WellEntity and its analytes.
 * Handles CSV and JSON formats.
 *
 * CSV format expected: each row represents one analyte measurement for a well.
 * Columns: name, latitude, longitude, analyte, concentration, dateSampled
 *
 * JSON format expected:
 * [
 *   {
 *     "name": "...",
 *     "latitude": ...,
 *     "longitude": ...,
 *     "analytes": [
 *        { "analyte": "...", "concentration": 0.02, "dateSampled": "2024-05-12" },
 *        ...
 *     ]
 *   },
 *   ...
 * ]
 */
object DataImporter {

    fun importFromAsset(context: Context, assetName: String): List<ImportedWell> {
        return when {
            assetName.endsWith(".csv", ignoreCase = true) -> importFromAssetCsv(context, assetName)
            assetName.endsWith(".json", ignoreCase = true) -> importFromAssetJson(context, assetName)
            else -> emptyList()
        }
    }

    /**
     * CSV importer:
     * Assumes each CSV row is one analyte measurement for a well.
     * We aggregate rows by well (by name+lat+lng) into ImportedWell objects.
     */
    fun importFromAssetCsv(context: Context, assetName: String = "sample_wells.csv"): List<ImportedWell> {
        val input = context.assets.open(assetName)
        val reader = BufferedReader(InputStreamReader(input))
        val map = LinkedHashMap<String, ImportedWellBuilder>() // key -> builder preserving insertion order

        reader.useLines { lines ->
            val it = lines.iterator()
            if (!it.hasNext()) return emptyList()
            val header = it.next() // skip header
            while (it.hasNext()) {
                val line = it.next().trim()
                if (line.isEmpty()) continue
                val parts = line.split(",")
                if (parts.size < 3) continue
                val name = parts.getOrNull(0)?.trim().orEmpty()
                val lat = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
                val lng = parts.getOrNull(2)?.trim()?.toDoubleOrNull() ?: 0.0
                val analyteName = parts.getOrNull(3)?.trim()?.ifBlank { null }
                val concentration = parts.getOrNull(4)?.trim()?.toDoubleOrNull()
                val dateSampled = parts.getOrNull(5)?.trim()?.ifBlank { null }

                // key by name+lat+lng
                val key = "$name|$lat|$lng"
                val builder = map.getOrPut(key) {
                    ImportedWellBuilder(
                        WellEntity(
                            name = name,
                            latitude = lat,
                            longitude = lng,
                            analyte = null,
                            concentration = null,
                            dateSampled = null
                        )
                    )
                }
                if (!analyteName.isNullOrBlank()) {
                    builder.analytes.add(
                        WellAnalyteEntity(
                            wellId = 0L,
                            analyteName = analyteName,
                            concentration = concentration,
                            dateSampled = dateSampled
                        )
                    )
                }
            }
        }
        return map.values.map { it.build() }
    }

    /**
     * JSON importer:
     * Expects array of well objects; each may include "analytes" array
     */
    fun importFromAssetJson(context: Context, assetName: String = "sample_wells.json"): List<ImportedWell> {
        val text = context.assets.open(assetName).bufferedReader().use { it.readText() }
        if (text.isBlank()) return emptyList()
        val arr = JSONArray(text)
        val out = mutableListOf<ImportedWell>()
        for (i in 0 until arr.length()) {
            val o: JSONObject = arr.getJSONObject(i)
            val name = o.optString("name", "Unknown")
            val lat = o.optDouble("latitude", 0.0)
            val lng = o.optDouble("longitude", 0.0)
            val well = WellEntity(
                name = name,
                latitude = lat,
                longitude = lng,
                analyte = null,
                concentration = null,
                dateSampled = null
            )
            val analytes = mutableListOf<WellAnalyteEntity>()
            if (o.has("analytes") && !o.isNull("analytes")) {
                val analyteArr = o.getJSONArray("analytes")
                for (j in 0 until analyteArr.length()) {
                    val aobj = analyteArr.getJSONObject(j)
                    val analyteName = aobj.optString("analyte", "")
                    val concentration = if (aobj.has("concentration") && !aobj.isNull("concentration")) {
                        aobj.optDouble("concentration")
                    } else null
                    val dateSampled = aobj.optString("dateSampled", null)
                    if (analyteName.isNotBlank()) {
                        analytes.add(
                            WellAnalyteEntity(
                                wellId = 0L,
                                analyteName = analyteName,
                                concentration = concentration,
                                dateSampled = dateSampled
                            )
                        )
                    }
                }
            }
            out.add(ImportedWell(well = well, analytes = analytes))
        }
        return out
    }

    // small builder used internally by CSV aggregator
    private class ImportedWellBuilder(val well: WellEntity) {
        val analytes = mutableListOf<WellAnalyteEntity>()
        fun build(): ImportedWell = ImportedWell(well = well, analytes = analytes.toList())
    }

    /**
     * Helper: parse JSON text and return ImportedWell list.
     * This duplicates importFromAssetJson logic but accepts a raw JSON string.
     */
    fun importFromJsonString(jsonText: String): List<ImportedWell> {
        // Reuse existing logic but operate on provided jsonText
        if (jsonText.isBlank()) return emptyList()
        val arr = org.json.JSONArray(jsonText)
        val out = mutableListOf<ImportedWell>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val name = o.optString("name", "Unknown")
            val lat = o.optDouble("latitude", 0.0)
            val lng = o.optDouble("longitude", 0.0)
            val well = WellEntity(
                name = name,
                latitude = lat,
                longitude = lng,
                analyte = null,
                concentration = null,
                dateSampled = null
            )
            val analytes = mutableListOf<WellAnalyteEntity>()
            if (o.has("analytes") && !o.isNull("analytes")) {
                val analyteArr = o.getJSONArray("analytes")
                for (j in 0 until analyteArr.length()) {
                    val aobj = analyteArr.getJSONObject(j)
                    val analyteName = aobj.optString("analyte", "")
                    val concentration = if (aobj.has("concentration") && !aobj.isNull("concentration")) {
                        aobj.optDouble("concentration")
                    } else null
                    val dateSampled = aobj.optString("dateSampled", null.toString())
                    if (analyteName.isNotBlank()) {
                        analytes.add(
                            WellAnalyteEntity(
                                wellId = 0L,
                                analyteName = analyteName,
                                concentration = concentration,
                                dateSampled = dateSampled
                            )
                        )
                    }
                }
            }
            out.add(ImportedWell(well = well, analytes = analytes))
        }
        return out
    }

    /**
     * Helper: parse CSV text and aggregate rows into ImportedWells.
     * Expects CSV header row; each row corresponds to a well-analyte line.
     */
    fun importFromCsvString(csvText: String): List<ImportedWell> {
        val lines = csvText.lines()
        if (lines.isEmpty()) return emptyList()
        val map = LinkedHashMap<String, MutableList<WellAnalyteEntity>>()
        val wells = LinkedHashMap<String, WellEntity>()

        // skip header
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val parts = line.split(",")
            if (parts.size < 3) continue
            val name = parts.getOrNull(0)?.trim().orEmpty()
            val lat = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
            val lng = parts.getOrNull(2)?.trim()?.toDoubleOrNull() ?: 0.0
            val analyteName = parts.getOrNull(3)?.trim()?.ifBlank { null }
            val concentration = parts.getOrNull(4)?.trim()?.toDoubleOrNull()
            val dateSampled = parts.getOrNull(5)?.trim()?.ifBlank { null }

            val key = "$name|$lat|$lng"
            if (!wells.containsKey(key)) {
                wells[key] = WellEntity(
                    name = name,
                    latitude = lat,
                    longitude = lng,
                    analyte = null,
                    concentration = null,
                    dateSampled = null
                )
            }
            if (!analyteName.isNullOrBlank()) {
                val list = map.getOrPut(key) { mutableListOf() }
                list.add(
                    WellAnalyteEntity(
                        wellId = 0L,
                        analyteName = analyteName,
                        concentration = concentration,
                        dateSampled = dateSampled
                    )
                )
            }
        }

        val out = mutableListOf<ImportedWell>()
        for ((k, well) in wells) {
            val analytes = map[k]?.toList() ?: emptyList()
            out.add(ImportedWell(well = well, analytes = analytes))
        }
        return out
    }

}
