package com.jibase.iflexible.common

import androidx.recyclerview.widget.OrientationHelper

interface IFlexibleLayoutManager {
    /**
     * Finds the layout orientation of the RecyclerView, no matter which LayoutManager is in use.
     *
     * @return one of [OrientationHelper.HORIZONTAL], [OrientationHelper.VERTICAL]
     */
    fun getOrientation(): Int

    /**
     * Helper method to retrieve the number of the columns (span count) of the given LayoutManager.
     *
     * All Layouts are supported.
     *
     * @return the span count
     */
    fun getSpanCount(): Int

    /**
     * Helper method to find the adapter position of the **first completely** visible view
     * [for each span], no matter which Layout is.
     *
     * @return the adapter position of the **first fully** visible item or `RecyclerView.NO_POSITION`
     * if there aren't any visible items.
     * @see .findFirstVisibleItemPosition
     */
    fun findFirstCompletelyVisibleItemPosition(): Int

    /**
     * Helper method to find the adapter position of the **first partially** visible view
     * [for each span], no matter which Layout is.
     *
     * @return the adapter position of the **first partially** visible item or `RecyclerView.NO_POSITION`
     * if there aren't any visible items.
     * @see .findFirstCompletelyVisibleItemPosition
     */
    fun findFirstVisibleItemPosition(): Int

    /**
     * Helper method to find the adapter position of the **last completely** visible view
     * [for each span], no matter which Layout is.
     *
     * @return the adapter position of the **last fully** visible item or `RecyclerView.NO_POSITION`
     * if there aren't any visible items.
     * @see .findLastVisibleItemPosition
     */
    fun findLastCompletelyVisibleItemPosition(): Int

    /**
     * Helper method to find the adapter position of the **last partially** visible view
     * [for each span], no matter which Layout is.
     *
     * @return the adapter position of the **last partially** visible item or `RecyclerView.NO_POSITION`
     * if there aren't any visible items.
     * @see .findLastCompletelyVisibleItemPosition
     */
    fun findLastVisibleItemPosition(): Int
}