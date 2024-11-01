package com.jibase.iflexible.utils

import androidx.annotation.DimenRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

object CenterItemRecyclerViewUtils {
    fun post(
        recyclerView: RecyclerView,
        position: Int,
        @DimenRes widthItemDimens: Int,
        @DimenRes marginItemDimens: Int = 0,
        @DimenRes prefixDimens: Int = 0,
        @DimenRes suffixDimens: Int = 0
    ) {
        val resources = recyclerView.context.resources
        val widthOfItem = resources.getDimension(widthItemDimens)
        val marginOfItem =
            if (marginItemDimens != 0) resources.getDimension(marginItemDimens) * 2 else 0f

        val prefix = if (prefixDimens != 0) resources.getDimension(prefixDimens) else 0f
        val suffix = if (suffixDimens != 0) resources.getDimension(suffixDimens) else 0f

        post(recyclerView, position, widthOfItem, marginOfItem, prefix, suffix)
    }

    fun post(
        recyclerView: RecyclerView,
        position: Int,
        widthItem: Float,
        marginItem: Float = 0f,
        prefix: Float = 0f,
        suffix: Float = 0f
    ) {
        val resources = recyclerView.context.resources
        val widthOfScreen = resources.displayMetrics.widthPixels
        val tWidthItem = widthItem + (marginItem * 2)
        val offset = (((widthOfScreen - tWidthItem) / 2f) - prefix + suffix).roundToInt()
        (recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
            position,
            offset
        )
    }
}