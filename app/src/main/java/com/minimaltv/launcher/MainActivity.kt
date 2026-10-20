package com.minimaltv.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minimaltv.launcher.data.AppLauncher
import com.minimaltv.launcher.data.AppOrder
import com.minimaltv.launcher.data.AppRepository
import com.minimaltv.launcher.data.PreferencesRepository
import com.minimaltv.launcher.data.AppVisibility
import com.minimaltv.launcher.model.TvApp
import com.minimaltv.launcher.ui.AppGridAdapter
import com.minimaltv.launcher.ui.AppIconLoader
import com.minimaltv.launcher.ui.FocusRestorer
import java.util.Date
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var grid: RecyclerView
    private lateinit var adapter: AppGridAdapter
    private lateinit var icons: AppIconLoader
    private lateinit var repository: AppRepository
    private lateinit var preferences: PreferencesRepository
    private lateinit var launcher: AppLauncher
    private var focusedPackage: String? = null
    private var focusedIndex = 0
    private var currentApps: List<TvApp> = emptyList()
    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockTick = object : Runnable {
        override fun run() {
            updateClock()
            clockHandler.postDelayed(this, CLOCK_UPDATE_MS)
        }
    }
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.data?.schemeSpecificPart?.let { packageName ->
                if (intent.action == Intent.ACTION_PACKAGE_REMOVED &&
                    !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                ) {
                    preferences.forgetPackage(packageName)
                }
            }
            refreshApps()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repository = AppRepository(this)
        preferences = PreferencesRepository(getSharedPreferences("minimal_tv", MODE_PRIVATE))
        launcher = AppLauncher(this, preferences)
        icons = AppIconLoader(this, worker)
        grid = findViewById(R.id.appGrid)
        val width = resources.displayMetrics.widthPixels
        val columns = if (width >= 1200) 6 else if (width >= 900) 5 else 4
        grid.layoutManager = GridLayoutManager(this, columns)
        grid.setHasFixedSize(true)
        adapter = AppGridAdapter(icons,
            onFocus = { app, index -> focusedPackage = app.packageName; focusedIndex = index },
            onOpen = { app -> openApp(app) },
            onLongPress = { app -> showFavoriteMenu(app) })
        grid.adapter = adapter
        findViewById<TextView>(R.id.favoritesAction).setOnClickListener { showFavoriteManagement() }
        findViewById<View>(R.id.topSettings).setOnClickListener { showSettings() }
        findViewById<TextView>(R.id.settingsAction).setOnClickListener { showSettings() }
        registerPackageReceiver()
    }

    override fun onResume() {
        super.onResume()
        clockHandler.removeCallbacks(clockTick)
        clockHandler.post(clockTick)
        refreshApps()
    }

    override fun onPause() {
        clockHandler.removeCallbacks(clockTick)
        super.onPause()
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)
        clockHandler.removeCallbacks(clockTick)
        worker.shutdownNow()
        icons.clear()
        super.onDestroy()
    }

    private fun registerPackageReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(packageReceiver, filter)
        }
    }

    private fun updateClock() {
        findViewById<TextView>(R.id.clock).text = DateFormat.getTimeFormat(this).format(Date())
    }

    private fun refreshApps() {
        worker.execute {
            val visibleApps = AppVisibility.filter(repository.loadApps(), HIDDEN_SYSTEM_APPS)
            val apps = AppOrder.sort(visibleApps, preferences.favorites(), preferences.launchCounts())
            runOnUiThread {
                if (!isFinishing && !isDestroyed) showApps(apps)
            }
        }
    }

    private fun showApps(apps: List<TvApp>) {
        currentApps = apps
        val initial = adapter.itemCount == 0
        val shouldRestore = initial || grid.hasFocus()
        adapter.submit(apps)
        if (shouldRestore && apps.isNotEmpty()) {
            val index = FocusRestorer.indexOf(focusedPackage, focusedIndex, apps)
            grid.scrollToPosition(index)
            grid.post {
                grid.findViewHolderForAdapterPosition(index)?.itemView?.requestFocus()
                    ?: grid.requestFocus()
            }
        }
    }

    private fun showFavoriteManagement() {
        val snapshot = currentApps
        val favorites = preferences.favorites()
        val names = snapshot.map { it.name }.toTypedArray()
        val checked = snapshot.map { it.packageName in favorites }.toBooleanArray()
        AlertDialog.Builder(this)
            .setTitle("Gerenciar favoritos")
            .setMultiChoiceItems(names, checked) { _, index, _ ->
                preferences.toggleFavorite(snapshot[index].packageName)
                refreshApps()
            }
            .setPositiveButton("Concluir", null)
            .show()
    }

    private fun showFavoriteMenu(app: TvApp) {
        val isFavorite = app.packageName in preferences.favorites()
        val action = if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos"
        AlertDialog.Builder(this)
            .setTitle(app.name)
            .setItems(arrayOf(action)) { _, _ ->
                preferences.toggleFavorite(app.packageName)
                refreshApps()
            }
            .setNegativeButton("Voltar", null)
            .show()
    }

    private fun openApp(app: TvApp) {
        if (!launcher.launch(app)) {
            Toast.makeText(this, "Não foi possível abrir ${app.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSettings() {
        AlertDialog.Builder(this)
            .setTitle("Configurações")
            .setMessage("Abrir configurações do Android TV")
            .setPositiveButton("Abrir") { _, _ ->
                try {
                    startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(this, "Configurações indisponíveis", Toast.LENGTH_SHORT).show()
                } catch (_: SecurityException) {
                    Toast.makeText(this, "Acesso às configurações bloqueado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Voltar", null)
            .show()
    }

    private companion object {
        const val CLOCK_UPDATE_MS = 60_000L
        val HIDDEN_SYSTEM_APPS = setOf(
            "com.mediatek.wwtv.tvcenter",
            "com.mediatek.wwtv.mediaplayer",
            "com.mediatek.androidbox"
        )
    }
}
