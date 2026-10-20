package com.minimaltv.launcher.ui

import com.minimaltv.launcher.model.TvApp

object FocusRestorer {
    fun indexOf(previousPackage: String?, previousIndex: Int, apps: List<TvApp>): Int {
        if (apps.isEmpty()) return -1
        val retained = apps.indexOfFirst { it.packageName == previousPackage }
        return if (retained >= 0) retained else previousIndex.coerceIn(0, apps.lastIndex)
    }
}
