package com.example.navajowatergis.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.navajowatergis.R
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOpenList).setOnClickListener {
            startActivity(Intent(this, WellListActivity::class.java))
        }
        findViewById<Button>(R.id.btnOpenMap).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
        findViewById<Button>(R.id.btnImport).setOnClickListener {
            startActivity(Intent(this, ImportActivity::class.java))
        }
        findViewById<Button>(R.id.btnAbout).setOnClickListener {
            // Simple about - opens a lightweight AboutActivity or show dialog. For now, show a simple activity if exists.
            // If not present, do nothing.
        }
    }
}
