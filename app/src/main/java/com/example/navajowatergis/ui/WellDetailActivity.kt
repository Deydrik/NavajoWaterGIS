package com.example.navajowatergis.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navajowatergis.R
import com.example.navajowatergis.data.WellDatabase
import com.example.navajowatergis.data.entities.WellWithAnalytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class WellDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WELL_ID = "extra_well_id"
    }

    private val tvName: TextView by lazy { findViewById(R.id.detail_name) }
    private val tvCoords: TextView by lazy { findViewById(R.id.detail_coords) }
    private val tvAnalyteSummary: TextView by lazy { findViewById(R.id.detail_analyte_summary) }
    private val rvAnalytes: RecyclerView by lazy { findViewById(R.id.recyclerAnalytes) }
    private val btnShare: Button by lazy { findViewById(R.id.btnShare) }

    private val analyteAdapter = WellAnalyteAdapter()
    private var currentWell: WellWithAnalytes? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_well_detail)

        rvAnalytes.adapter = analyteAdapter
        rvAnalytes.layoutManager = LinearLayoutManager(this)

        val id = intent.getLongExtra(EXTRA_WELL_ID, -1L)
        if (id == -1L) {
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = WellDatabase.getInstance(applicationContext).wellDao()
            val gwa = dao.getWellWithAnalytesById(id)
            if (gwa == null) {
                withContext(Dispatchers.Main) { finish() }
                return@launch
            }
            currentWell = gwa
            withContext(Dispatchers.Main) {
                tvName.text = gwa.well.name
                tvCoords.text = "Lat: ${gwa.well.latitude}, Lng: ${gwa.well.longitude}"
                val summary = if (gwa.analytes.isEmpty()) "No analytes" else gwa.analytes.joinToString(", ") { it.analyteName }
                tvAnalyteSummary.text = summary
                analyteAdapter.submitList(gwa.analytes)
            }
        }

        btnShare.setOnClickListener {
            currentWell?.let { shareWellAsJson(it) }
        }
    }

    private fun shareWellAsJson(gwa: WellWithAnalytes) {
        val root = JSONObject().apply {
            put("id", gwa.well.id)
            put("name", gwa.well.name)
            put("latitude", gwa.well.latitude)
            put("longitude", gwa.well.longitude)
            val analyteArray = JSONArray()
            for (a in gwa.analytes) {
                val ao = JSONObject()
                ao.put("analyte", a.analyteName)
                a.concentration?.let { ao.put("concentration", it) }
                a.dateSampled?.let { ao.put("dateSampled", it) }
                analyteArray.put(ao)
            }
            put("analytes", analyteArray)
        }

        val txt = root.toString(2) // pretty-print

        val share = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "Well export: ${gwa.well.name}")
            putExtra(Intent.EXTRA_TEXT, txt)
        }
        startActivity(Intent.createChooser(share, "Share well data"))
    }
}
