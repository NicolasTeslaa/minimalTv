package com.minimaltv.launcher.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.minimaltv.launcher.model.TvApp
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class AppGridAdapterTest {
    @Test fun shortOkTriggersOpenCallback() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val executor = Executors.newSingleThreadExecutor()
        try {
            var opened = 0
            val adapter = AppGridAdapter(
                AppIconLoader(context, executor),
                onFocus = { _, _ -> },
                onOpen = { opened++ },
                onLongPress = { }
            )
            val parent = RecyclerView(context)
            parent.layoutManager = GridLayoutManager(context, 4)
            val app = TvApp("com.example.video", "Video", "com.example.video/.Main")
            instrumentation.runOnMainSync {
                adapter.submit(listOf(app))
                val holder = adapter.onCreateViewHolder(parent, 0)
                adapter.onBindViewHolder(holder, 0)
                holder.itemView.performClick()
            }
            assertEquals(1, opened)
        } finally { executor.shutdownNow() }
    }

    @Test fun longOkDoesNotAlsoTriggerShortOk() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val executor = Executors.newSingleThreadExecutor()
        try {
            var opened = 0
            var longPressed = 0
            val adapter = AppGridAdapter(
                AppIconLoader(context, executor),
                onFocus = { _, _ -> },
                onOpen = { opened++ },
                onLongPress = { longPressed++ }
            )
            val parent = RecyclerView(context)
            parent.layoutManager = GridLayoutManager(context, 4)
            val app = TvApp("com.example.video", "Video", "com.example.video/.Main")
            instrumentation.runOnMainSync {
                adapter.submit(listOf(app))
                val holder = adapter.onCreateViewHolder(parent, 0)
                adapter.onBindViewHolder(holder, 0)
                holder.itemView.performLongClick()
                holder.itemView.performClick()
            }
            assertEquals(1, longPressed)
            assertEquals(0, opened)
        } finally { executor.shutdownNow() }
    }
}
