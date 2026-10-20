package com.minimaltv.launcher.ui

import com.minimaltv.launcher.model.TvApp
import org.junit.Assert.assertEquals
import org.junit.Test

class FocusRestorerTest {
    private val apps = listOf(
        TvApp("a", "A", "a.Main"),
        TvApp("b", "B", "b.Main"),
        TvApp("c", "C", "c.Main")
    )

    @Test fun keepsSameAppFocusedAfterReorder() {
        assertEquals(2, FocusRestorer.indexOf("c", 0, apps))
    }

    @Test fun choosesNeighborWhenFocusedAppDisappears() {
        assertEquals(1, FocusRestorer.indexOf("missing", 2, apps.take(2)))
    }

    @Test fun emptyGridHasNoFocusedItem() {
        assertEquals(-1, FocusRestorer.indexOf("missing", 0, emptyList()))
    }
}
