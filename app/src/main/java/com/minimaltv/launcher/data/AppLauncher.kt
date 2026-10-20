package com.minimaltv.launcher.data

import android.content.Context
import android.content.ComponentName
import android.content.Intent
import android.content.ActivityNotFoundException
import com.minimaltv.launcher.model.TvApp

class AppLauncher(private val context: Context, private val preferences: PreferencesRepository) {
    fun launch(app: TvApp): Boolean {
        val component = ComponentName.unflattenFromString(app.componentName) ?: return false
        val intent = Intent(Intent.ACTION_MAIN).setComponent(component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            preferences.recordSuccessfulLaunch(app.packageName)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}
