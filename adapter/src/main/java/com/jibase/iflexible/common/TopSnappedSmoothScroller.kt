package com.jibase.iflexible.common

import android.content.Context
import android.graphics.PointF
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper

class TopSnappedSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    /**
     * The modification of this value affects the creation of ALL Layout Managers.
     * **Note:** Every time you change this value you MUST recreate the LayoutManager instance
     * and to assign it again to the RecyclerView!
     *
     * Default value is `100f`. Default Android value is `25f`.
     */
    private var MILLISECONDS_PER_INCH = 100f

    private val vectorPosition = PointF(0f, 0f)
    private val flexibleLayoutManager: IFlexibleLayoutManager

    init {
        flexibleLayoutManager = FlexibleLayoutManager(layoutManager!!)
    }

    /**
     * Controls the direction in which smoothScroll looks for your view
     *
     * @return the vector position
     */
    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        val firstChildPos = flexibleLayoutManager.findFirstCompletelyVisibleItemPosition()
        val direction = if (targetPosition < firstChildPos) -1 else 1

        return if (flexibleLayoutManager.getOrientation() == OrientationHelper.HORIZONTAL) {
            vectorPosition.set(direction.toFloat(), 0f)
            vectorPosition
        } else {
            vectorPosition.set(0f, direction.toFloat())
            vectorPosition
        }
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
    }

    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
    }
}