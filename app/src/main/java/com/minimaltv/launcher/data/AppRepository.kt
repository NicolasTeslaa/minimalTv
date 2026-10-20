package com.minimaltv.launcher.data

import android.content.Context
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import com.minimaltv.launcher.model.TvApp

class AppRepository(private val context: Context) {
    fun loadApps(): List<TvApp> = AppSelection.select(
        leanback = query(Intent.CATEGORY_LEANBACK_LAUNCHER),
        standard = query(Intent.CATEGORY_LAUNCHER),
        ownPackage = context.packageName
    )

    private fun query(category: String): List<LaunchCandidate> {
        val manager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        val resolved = if (android.os.Build.VERSION.SDK_INT >= 33) {
            manager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            manager.queryIntentActivities(intent, 0)
        }
        return resolved.mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            LaunchCandidate(
                packageName = activity.packageName,
                name = info.loadLabel(manager)?.toString()?.trim().orEmpty()
                    .ifEmpty { activity.packageName },
                componentName = ComponentName(activity.packageName, activity.name).flattenToString(),
                enabled = activity.enabled && activity.applicationInfo.enabled
            )
        }
    }
}
