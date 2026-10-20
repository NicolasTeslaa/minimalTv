package com.minimaltv.launcher.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.minimaltv.launcher.model.TvApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLauncherTest {
    private val app = TvApp("com.example.video", "Video", "com.example.video/.TvActivity")

    @Test fun successfulOpenUsesResolvedComponentAndCountsOnce() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("test_launcher", 0)
        prefs.edit().clear().commit()
        var launched: Intent? = null
        val starter = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent) { launched = intent }
        }
        try {
            val repository = PreferencesRepository(prefs)
            assertTrue(AppLauncher(starter, repository).launch(app))
            assertEquals("com.example.video", launched?.component?.packageName)
            assertEquals("com.example.video.TvActivity", launched?.component?.className)
            assertEquals(Intent.ACTION_MAIN, launched?.action)
            assertTrue(Intent.CATEGORY_LEANBACK_LAUNCHER in (launched?.categories ?: emptySet()))
            assertEquals(1, repository.launchCounts()[app.packageName])
        } finally { prefs.edit().clear().commit() }
    }

    @Test fun failedOpenDoesNotCountUsage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("test_launcher", 0)
        prefs.edit().clear().commit()
        val starter = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent) { throw ActivityNotFoundException() }
        }
        try {
            val repository = PreferencesRepository(prefs)
            assertFalse(AppLauncher(starter, repository).launch(app))
            assertEquals(null, repository.launchCounts()[app.packageName])
        } finally { prefs.edit().clear().commit() }
    }
}
