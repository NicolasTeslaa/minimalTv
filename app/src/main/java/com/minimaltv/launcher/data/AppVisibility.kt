package com.minimaltv.launcher.data

import com.minimaltv.launcher.model.TvApp

object AppVisibility {
    fun filter(apps: List<TvApp>, hiddenPackages: Set<String>): List<TvApp> =
        apps.filterNot { it.packageName in hiddenPackages }
}
