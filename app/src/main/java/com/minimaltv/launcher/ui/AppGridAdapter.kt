package com.minimaltv.launcher.ui

import android.view.LayoutInflater
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.minimaltv.launcher.R
import com.minimaltv.launcher.model.TvApp

class AppGridAdapter(
    private val iconLoader: AppIconLoader,
    private val onFocus: (TvApp, Int) -> Unit,
    private val onOpen: (TvApp) -> Unit,
    private val onLongPress: (TvApp) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.Holder>() {
    private var items: List<TvApp> = emptyList()

    init { setHasStableIds(true) }

    override fun getItemId(position: Int): Long = items[position].packageName.hashCode().toLong()
    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val app = items[position]
        holder.app = app
        holder.name.text = app.name
        holder.itemView.contentDescription = app.name
        iconLoader.load(app.packageName, holder.icon)
    }

    fun submit(next: List<TvApp>) {
        val old = items
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = old.size
            override fun getNewListSize() = next.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                old[oldItemPosition].packageName == next[newItemPosition].packageName
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                old[oldItemPosition] == next[newItemPosition]
        })
        items = next
        diff.dispatchUpdatesTo(this)
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        lateinit var app: TvApp
        private var suppressNextClick = false

        init {
            view.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 &&
                    (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER ||
                        keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
                    suppressNextClick = false
                }
                false
            }
            view.setOnFocusChangeListener { _, focused ->
                if (focused && ::app.isInitialized) onFocus(app, bindingAdapterPosition)
            }
            view.setOnClickListener {
                if (suppressNextClick) suppressNextClick = false
                else if (::app.isInitialized) onOpen(app)
            }
            view.setOnLongClickListener {
                suppressNextClick = true
                if (::app.isInitialized) onLongPress(app)
                true
            }
        }
    }
}
