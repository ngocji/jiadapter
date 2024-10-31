package com.jibase.iflexible.utils

import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.jibase.iflexible.adapter.FlexibleAdapter.Companion.MULTI
import com.jibase.iflexible.adapter.FlexibleAdapter.Companion.SINGLE
import com.jibase.iflexible.common.FlexibleLayoutManager

object LayoutUtils {
    /**
     * @ return the string representation of the provided
     */
    @JvmStatic
    fun getModeName(mode: Int): String {
        return when (mode) {
            SINGLE -> "SINGLE"
            MULTI -> "MULTI"
            else -> "IDLE"
        }
    }
    /*-------------------------------*/
    /* RECYCLER-VIEW UTILITY METHODS */
    /*-------------------------------*/
    /**
     * Finds the layout orientation of the RecyclerView, no matter which LayoutManager is in use.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return one of [OrientationHelper.HORIZONTAL], [OrientationHelper.VERTICAL]
     */
    @JvmStatic
    fun getOrientation(recyclerView: RecyclerView): Int {
        return FlexibleLayoutManager(recyclerView).getOrientation()
    }

    /**
     * Helper method to retrieve the number of the columns (span count) of the given LayoutManager.
     *
     * All Layouts are supported.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the span count
     */
    @JvmStatic
    fun getSpanCount(recyclerView: RecyclerView): Int {
        return FlexibleLayoutManager(recyclerView).getSpanCount()
    }

    /**
     * Helper method to find the adapter position of the **first completely** visible view
     * [for each span], no matter which Layout is.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the adapter position of the **first fully** visible item or `RecyclerView.NO_POSITION`
     * if there aren't any visible items.
     * @see .findFirstVisibleItemPosition
     */
    @JvmStatic
    fun findFirstCompletelyVisibleItemPosition(recyclerView: RecyclerView): Int {
        return FlexibleLayoutManager(recyclerView).findFirstCompletelyVisibleItemPosition()
    }

    /**
     * Helper method to find the adapter position of the **first partially** visible view
     * [for each span], no matter which Layout is.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the adapter position of the **first partially** visible item or `RecyclerView.NO_POSITION`
     * if there aren't any visible items.
     * @see .findFirstCompletelyVisibleItemPosition
     */
    @JvmStatic
    fun findFirstVisibleItemPosition(recyclerView: RecyclerView): Int {
        return FlexibleLayoutManager(recyclerView).findFirstVisibleItemPosition()
    }

    /**
     * Helper method to find the adapter position of the **last completely** visible view
     * [for each span], no matter which Layout is.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the adapter position of the **last fully** visible item or `RecyclerView.NO_POSITION`
     * if there aren't any visible items.
     * @see .findLastVisibleItemPosition
     */
    @JvmStatic
    fun findLastCompletelyVisibleItemPosition(recyclerView: RecyclerView): Int {
        return FlexibleLayoutManager(recyclerView).findLastCompletelyVisibleItemPosition()
    }

    /**
     * Helper method to find the adapter position of the **last partially** visible view
     * [for each span], no matter which Layout is.
     *
     * @param recyclerView the RecyclerView with LayoutManager instance in use
     * @return the adapter position of the **last partially** visible item or `RecyclerView.NO_POSITION`
     * if there aren't any visible items.
     * @see .findLastCompletelyVisibleItemPosition
     */
    @JvmStatic
    fun findLastVisibleItemPosition(recyclerView: RecyclerView): Int {
        return FlexibleLayoutManager(recyclerView).findLastVisibleItemPosition()
    }

}