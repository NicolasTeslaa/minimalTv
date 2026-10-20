package com.minimaltv.launcher.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryTest {
    @Test fun favoriteAndLaunchCountSurviveRepositoryRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("test_preferences", 0)
        prefs.edit().clear().commit()
        try {
            val first = PreferencesRepository(prefs)
            first.toggleFavorite("com.example.video")
            first.recordSuccessfulLaunch("com.example.video")
            first.recordSuccessfulLaunch("com.example.video")

            val second = PreferencesRepository(prefs)
            assertTrue("com.example.video" in second.favorites())
            assertEquals(2, second.launchCounts()["com.example.video"])
        } finally {
            prefs.edit().clear().commit()
        }
    }

    @Test fun removalClearsFavoriteAndCountForLaterReinstall() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("test_preferences", 0)
        prefs.edit().clear().commit()
        try {
            val repository = PreferencesRepository(prefs)
            repository.toggleFavorite("com.example.video")
            repository.recordSuccessfulLaunch("com.example.video")
            repository.forgetPackage("com.example.video")

            val recreated = PreferencesRepository(prefs)
            assertFalse("com.example.video" in recreated.favorites())
            assertEquals(null, recreated.launchCounts()["com.example.video"])
        } finally {
            prefs.edit().clear().commit()
        }
    }
}
