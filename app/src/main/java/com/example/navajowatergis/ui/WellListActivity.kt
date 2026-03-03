package com.example.navajowatergis.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navajowatergis.R
import com.example.navajowatergis.data.entities.WellWithAnalytes
import com.example.navajowatergis.utils.LocationHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Toast

class WellListActivity : AppCompatActivity() {

    private val viewModel: WellViewModel by viewModels()
    private lateinit var adapter: WellListAdapter
    private val locationHelper by lazy { LocationHelper(this) }

    private lateinit var emptyState: TextView
    private lateinit var btnNearest: Button

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                findNearestAndScroll()
            } else {
                Toast.makeText(this, "Location permission required for nearest-well", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_well_list)

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val fab = findViewById<FloatingActionButton>(R.id.mapFab)
        emptyState = findViewById(R.id.empty_state)
        btnNearest = findViewById(R.id.btnNearest)

        adapter = WellListAdapter { gwa -> openDetail(gwa) }
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        viewModel.wellsLive.observe(this) { list ->
            adapter.submitList(list)
            emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.preloadSampleDataIfNeeded()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        fab.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        btnNearest.setOnClickListener {
            // Request permission and then find nearest
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                findNearestAndScroll()
            }
        }
    }

    private fun openDetail(gwa: WellWithAnalytes) {
        val i = Intent(this, WellDetailActivity::class.java)
        i.putExtra(WellDetailActivity.EXTRA_WELL_ID, gwa.well.id)
        startActivity(i)
    }

    private fun findNearestAndScroll() {
        locationHelper.getLastKnownLocation { location: Location? ->
            if (location == null) {
                runOnUiThread {
                    Toast.makeText(this, "Unable to obtain location", Toast.LENGTH_SHORT).show()
                }
                return@getLastKnownLocation
            }
            // set user location in viewmodel so sorting by nearest works
            viewModel.setUserLocation(location.latitude, location.longitude)
            viewModel.setSortMode(WellViewModel.SortMode.NEAREST)

            // after list updates, scroll to first item (nearest)
            // adopt a short delay to let live data propagate; alternative is to observe once
            viewModel.wellsLive.observe(this) { list ->
                if (list.isNotEmpty()) {
                    // find index of nearest (first item after sort)
                    val idx = 0
                    val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
                    recycler.post {
                        recycler.smoothScrollToPosition(idx)
                        Toast.makeText(this, "Scrolled to nearest well", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No wells available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
