package com.minimaltv.launcher.data

import android.content.SharedPreferences

class PreferencesRepository(private val prefs: SharedPreferences) {
    fun favorites(): Set<String> = prefs.getStringSet(FAVORITES, emptySet())?.toSet() ?: emptySet()

    fun launchCounts(): Map<String, Int> = prefs.all.mapNotNull { (key, value) ->
        if (key.startsWith(LAUNCH_PREFIX) && value is Int) {
            key.removePrefix(LAUNCH_PREFIX) to value
        } else null
    }.toMap()

    fun toggleFavorite(packageName: String) {
        val updated = favorites().toMutableSet()
        if (!updated.add(packageName)) updated.remove(packageName)
        prefs.edit().putStringSet(FAVORITES, updated).apply()
    }

    fun recordSuccessfulLaunch(packageName: String) {
        val key = LAUNCH_PREFIX + packageName
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    fun forgetPackage(packageName: String) {
        prefs.edit()
            .putStringSet(FAVORITES, favorites() - packageName)
            .remove(LAUNCH_PREFIX + packageName)
            .apply()
    }

    private companion object {
        const val FAVORITES = "favorites"
        const val LAUNCH_PREFIX = "launch."
    }
}
