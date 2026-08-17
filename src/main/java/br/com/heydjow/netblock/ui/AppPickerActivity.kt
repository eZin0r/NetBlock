package br.com.heydjow.netblock.ui

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.heydjow.netblock.R
import br.com.heydjow.netblock.data.FilterStatePrefs
import br.com.heydjow.netblock.ui.adapter.AppPickerAdapter

/*
class AppPickerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        val listView = findViewById<ListView>(R.id.listApps)
        val btnSave = findViewById<Button>(R.id.btnSaveApps)

        val pm = packageManager

        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }

        val adapter = AppPickerAdapter(applicationContext, apps)
        listView.adapter = adapter

        btnSave.setOnClickListener {
            val selectedPkgs = adapter.getSelectedPackages()
            FilterStatePrefs.setSelectedPackages(applicationContext, selectedPkgs)
            finish()
        }
    }
}
*/
class AppPickerActivity : Activity() {

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_app_picker)

        val recycler = findViewById<RecyclerView>(R.id.recyclerApps)
        recycler.layoutManager = LinearLayoutManager(this)

        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }

        recycler.adapter = AppPickerAdapter(this, apps)
    }
}
