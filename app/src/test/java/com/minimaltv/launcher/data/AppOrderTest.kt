package com.minimaltv.launcher.data

import com.minimaltv.launcher.model.TvApp
import org.junit.Assert.assertEquals
import org.junit.Test

class AppOrderTest {
    @Test fun favoritesComeFirstByLaunchCountAndOthersAlphabetically() {
        val apps = listOf(
            TvApp("c", "Cinema", "c.Main"),
            TvApp("a", "Áudio", "a.Main"),
            TvApp("b", "Browser", "b.Main"),
            TvApp("d", "Desenhos", "d.Main")
        )

        val ordered = AppOrder.sort(apps, setOf("a", "b"), mapOf("a" to 2, "b" to 4))

        assertEquals(listOf("b", "a", "c", "d"), ordered.map { it.packageName })
    }

    @Test fun tiedFavoritesUseNameThenPackageName() {
        val apps = listOf(
            TvApp("z", "Beta", "z.Main"),
            TvApp("a", "Alpha", "a.Main"),
            TvApp("b", "Alpha", "b.Main")
        )

        val ordered = AppOrder.sort(apps, apps.map { it.packageName }.toSet(), emptyMap())

        assertEquals(listOf("a", "b", "z"), ordered.map { it.packageName })
    }
}
