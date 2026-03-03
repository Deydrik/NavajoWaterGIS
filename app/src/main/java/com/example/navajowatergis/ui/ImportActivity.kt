package com.example.navajowatergis.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.navajowatergis.R
import com.example.navajowatergis.data.ImportedWell
import com.example.navajowatergis.data.WellDatabase
import com.example.navajowatergis.data.WellRepository
import com.example.navajowatergis.utils.DataImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var previewText: TextView
    private lateinit var btnPick: Button
    private lateinit var btnImport: Button
    private var pickedUri: Uri? = null
    private var previewSummary: String? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pickedUri = uri
        showPickedFile(uri)
        generatePreview(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        statusText = findViewById(R.id.import_status)
        previewText = findViewById(R.id.import_preview)
        btnPick = findViewById(R.id.btnPickFile)
        btnImport = findViewById(R.id.btnImportFile)

        btnPick.setOnClickListener {
            // Allow CSV and JSON mime types but not restricted
            pickFileLauncher.launch(arrayOf("application/json", "text/*", "text/csv", "text/plain"))
        }

        btnImport.setOnClickListener {
            val uri = pickedUri
            if (uri == null) {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            confirmAndImport(uri)
        }
    }

    private fun showPickedFile(uri: Uri) {
        val cursor = contentResolver.query(uri, null, null, null, null)
        var name = "Unknown file"
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        statusText.text = "Selected: $name"
    }

    private fun generatePreview(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // read up to first 2000 chars or a few lines for preview
                val input = contentResolver.openInputStream(uri) ?: throw Exception("Unable to open file")
                val reader = BufferedReader(InputStreamReader(input))
                val sb = StringBuilder()
                var lines = 0
                reader.useLines { sequence ->
                    for (line in sequence) {
                        sb.append(line).append("\n")
                        lines++
                        if (lines >= 10) break
                    }
                }
                val sample = sb.toString()
                previewSummary = sample
                withContext(Dispatchers.Main) {
                    previewText.text = sample
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    previewText.text = "Error creating preview: ${e.message}"
                }
            }
        }
    }

    private fun confirmAndImport(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("Import data")
            .setMessage("Importing will add records to the local database. Do you want to continue?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Import") { _, _ ->
                performImport(uri)
            }
            .show()
    }

    private fun performImport(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Read the file content into a temporary buffer and decide type
                val input = contentResolver.openInputStream(uri) ?: throw Exception("Unable to open file")
                val text = input.bufferedReader().use { it.readText() }.trim()
                if (text.isBlank()) throw Exception("File is empty")

                // Decide format by extension or content
                val name = uri.lastPathSegment ?: ""
                val isJson = name.endsWith(".json", ignoreCase = true) || text.trimStart().startsWith("[") || text.trimStart().startsWith("{")
                val isCsv = name.endsWith(".csv", ignoreCase = true) || text.contains(",") && text.lines().firstOrNull()?.contains(",") == true

                val imported: List<ImportedWell> = if (isJson) {
                    DataImporter.importFromJsonString(text)
                } else if (isCsv) {
                    DataImporter.importFromCsvString(text)
                } else {
                    throw Exception("Unknown file format")
                }

                val dao = WellDatabase.getInstance(applicationContext).wellDao()
                val repo = WellRepository(dao)
                // Insert imported wells. repository will set analyte wellId correctly
                repo.insertImportedWells(imported)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ImportActivity, "Import successful: ${imported.size} wells", Toast.LENGTH_LONG).show()
                    statusText.text = "Import complete: ${imported.size} wells added"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ImportActivity, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                    statusText.text = "Import failed: ${e.message}"
                }
            }
        }
    }
}
