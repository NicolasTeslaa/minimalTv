package com.minimaltv.launcher.data

import com.minimaltv.launcher.model.TvApp
import org.junit.Assert.assertEquals
import org.junit.Test

class AppVisibilityTest {
    @Test fun hidesOnlyConfiguredPackagesFromHome() {
        val apps = listOf(
            TvApp("com.mediatek.wwtv.tvcenter", "Live TV", "live.Main"),
            TvApp("com.netflix.ninja", "Netflix", "netflix.Main"),
            TvApp("com.mediatek.androidbox", "Wi-Fi Display Sink", "wfd.Main")
        )

        val visible = AppVisibility.filter(
            apps,
            setOf("com.mediatek.wwtv.tvcenter", "com.mediatek.androidbox")
        )

        assertEquals(listOf("com.netflix.ninja"), visible.map { it.packageName })
    }
}
