package com.jibase.iflexible.viewholder

import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.jibase.extensions.changeBackground
import com.jibase.extensions.changeElevation
import com.jibase.extensions.getCurrentElevation
import com.jibase.iflexible.adapter.FlexibleAdapter

abstract class AbstractContentViewHolder(view: View, val adapter: FlexibleAdapter<*>, private val isStickyHeader: Boolean) : RecyclerView.ViewHolder(if (isStickyHeader) FrameLayout(view.context) else view) {
    var contentView: View
    var backupPosition = RecyclerView.NO_POSITION

    init {
        if (isStickyHeader) {
            itemView.layoutParams = adapter.getRecyclerView().layoutManager?.generateLayoutParams(view.layoutParams)
            (itemView as FrameLayout).addView(view) //Add View after setLayoutParams
            val elevation = view.getCurrentElevation()
            if (elevation > 0) {
                itemView.apply {
                    changeBackground(background)
                    changeElevation(elevation)
                }
            }
            contentView = view
        } else {
            contentView = itemView
        }
    }

    fun getFlexibleAdapterPosition(): Int {
        var position = adapterPosition
        if (position == RecyclerView.NO_POSITION) {
            position = backupPosition
        }
        return position
    }
}