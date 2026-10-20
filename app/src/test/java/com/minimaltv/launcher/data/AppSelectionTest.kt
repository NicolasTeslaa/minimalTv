package com.minimaltv.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSelectionTest {
    @Test fun leanbackActivityWinsAndOrdinaryLauncherIsFallback() {
        val selected = AppSelection.select(
            leanback = listOf(LaunchCandidate("video", "Video TV", "video.Tv", true)),
            standard = listOf(
                LaunchCandidate("video", "Video", "video.Phone", true),
                LaunchCandidate("store", "Store", "store.Main", true)
            ),
            ownPackage = "minimal"
        )

        assertEquals(listOf("video.Tv", "store.Main"), selected.map { it.componentName })
    }

    @Test fun disabledSelfAndDuplicateActivitiesDoNotAppear() {
        val selected = AppSelection.select(
            leanback = listOf(
                LaunchCandidate("minimal", "Minimal TV", "minimal.Main", true),
                LaunchCandidate("video", "Video", "video.Disabled", false),
                LaunchCandidate("video", "Video", "video.Tv", true),
                LaunchCandidate("video", "Video", "video.Tv2", true)
            ),
            standard = emptyList(),
            ownPackage = "minimal"
        )

        assertEquals(listOf("video.Tv"), selected.map { it.componentName })
    }
}
