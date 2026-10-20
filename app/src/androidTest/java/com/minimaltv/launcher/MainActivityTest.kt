package com.minimaltv.launcher

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.minimaltv.launcher.ui.GradientBackgroundView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Test fun homeUsesGradientBackdrop() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val activity = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as MainActivity
        try {
            assertTrue(activity.findViewById<View>(R.id.mainRoot) is GradientBackgroundView)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test fun longOkOffersFavoriteAction() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.getSharedPreferences("minimal_tv", 0).edit().clear().commit()
        val activity = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as MainActivity
        try {
            val grid = activity.findViewById<RecyclerView>(R.id.appGrid)
            val deadline = System.currentTimeMillis() + 8_000
            while ((grid.adapter?.itemCount ?: 0) == 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(100)
                instrumentation.waitForIdleSync()
            }
            instrumentation.runOnMainSync {
                grid.findViewHolderForAdapterPosition(0)?.itemView?.performLongClick()
            }
            assertTrue(UiDevice.getInstance(instrumentation).wait(
                Until.hasObject(By.text("Adicionar aos favoritos")), 3_000))
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
            context.getSharedPreferences("minimal_tv", 0).edit().clear().commit()
        }
    }

    @Test fun settingsButtonOffersAndroidTvSettings() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val activity = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as MainActivity
        try {
            instrumentation.runOnMainSync {
                activity.findViewById<View>(R.id.topSettings).performClick()
            }
            assertTrue(UiDevice.getInstance(instrumentation).wait(
                Until.hasObject(By.text("Abrir configurações do Android TV")), 3_000))
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test fun favoritesFooterOpensManagementDialog() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val activity = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as MainActivity
        try {
            instrumentation.runOnMainSync {
                activity.findViewById<View>(R.id.favoritesAction).performClick()
            }
            assertTrue(UiDevice.getInstance(instrumentation).wait(Until.hasObject(By.text("CONCLUIR")), 3_000))
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test fun homeShowsSixColumnGridWithInitialFocus() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val activity = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ) as MainActivity
        try {
            val grid = activity.findViewById<RecyclerView>(R.id.appGrid)
            assertNotNull(grid)
            assertEquals(6, (grid.layoutManager as GridLayoutManager).spanCount)
            val deadline = System.currentTimeMillis() + 8_000
            while ((grid.adapter?.itemCount ?: 0) == 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(100)
                instrumentation.waitForIdleSync()
            }
            assertTrue((grid.adapter?.itemCount ?: 0) > 0)
            assertTrue(grid.hasFocus() || grid.findFocus() != null)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
