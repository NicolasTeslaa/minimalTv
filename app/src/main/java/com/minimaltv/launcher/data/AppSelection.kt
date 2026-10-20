package com.minimaltv.launcher.data

import com.minimaltv.launcher.model.TvApp

data class LaunchCandidate(
    val packageName: String,
    val name: String,
    val componentName: String,
    val enabled: Boolean
)

object AppSelection {
    fun select(
        leanback: List<LaunchCandidate>,
        standard: List<LaunchCandidate>,
        ownPackage: String
    ): List<TvApp> = (leanback.asSequence() + standard.asSequence())
        .filter { it.enabled && it.packageName != ownPackage }
        .distinctBy { it.packageName }
        .map { TvApp(it.packageName, it.name, it.componentName) }
        .toList()
}
