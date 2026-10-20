package com.minimaltv.launcher.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppRepositoryTest {
    @Test fun discoversInstalledTvAppsWithoutListingSelf() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val apps = AppRepository(context).loadApps()
        val packages = apps.map { it.packageName }

        assertTrue("Netflix must be discoverable on the target TV", "com.netflix.ninja" in packages)
        assertTrue("Play Store must be discoverable on the target TV", "com.android.vending" in packages)
        assertFalse(context.packageName in packages)
        assertTrue(apps.all { it.name.isNotBlank() && it.componentName.isNotBlank() })
    }
}
