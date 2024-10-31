package com.jibase.iflexible.common

import androidx.recyclerview.widget.*

class FlexibleLayoutManager(private val currentLayoutManager: RecyclerView.LayoutManager) : IFlexibleLayoutManager {
    constructor(recyclerView: RecyclerView) : this(recyclerView.layoutManager!!)

    override fun getOrientation(): Int {
        return when (currentLayoutManager) {
            is LinearLayoutManager -> currentLayoutManager.orientation
            is StaggeredGridLayoutManager -> currentLayoutManager.orientation
            else -> OrientationHelper.VERTICAL
        }
    }

    override fun getSpanCount(): Int {
        return when(currentLayoutManager){
            is GridLayoutManager -> currentLayoutManager.spanCount
            is StaggeredGridLayoutManager -> currentLayoutManager.spanCount
            else -> 1
        }
    }

    override fun findFirstCompletelyVisibleItemPosition(): Int {
        return if (currentLayoutManager is StaggeredGridLayoutManager) {
            val staggeredGLM = currentLayoutManager
            var position = staggeredGLM.findFirstCompletelyVisibleItemPositions(null)[0]
            for (i in 1 until getSpanCount()) {
                val nextPosition = staggeredGLM.findFirstCompletelyVisibleItemPositions(null)[i]
                if (nextPosition < position) position = nextPosition
            }
            position
        } else {
            (currentLayoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        }
    }

    override fun findFirstVisibleItemPosition(): Int {
        return if (currentLayoutManager is StaggeredGridLayoutManager) {
            val staggeredGLM = currentLayoutManager
            var position = staggeredGLM.findFirstVisibleItemPositions(null)[0]
            for (i in 1 until getSpanCount()) {
                val nextPosition = staggeredGLM.findFirstVisibleItemPositions(null)[i]
                if (nextPosition < position) position = nextPosition
            }
            position
        } else {
            (currentLayoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }
    }

    override fun findLastCompletelyVisibleItemPosition(): Int {
        val layoutManager = currentLayoutManager
        return if (layoutManager is StaggeredGridLayoutManager) {
            var position = layoutManager.findLastCompletelyVisibleItemPositions(null)[0]
            for (i in 1 until getSpanCount()) {
                val nextPosition = layoutManager.findLastCompletelyVisibleItemPositions(null)[i]
                if (nextPosition > position) position = nextPosition
            }
            position
        } else {
            (layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
        }
    }

    override fun findLastVisibleItemPosition(): Int {
        return if (currentLayoutManager is StaggeredGridLayoutManager) {
            val staggeredGLM = currentLayoutManager
            var position = staggeredGLM.findLastVisibleItemPositions(null)[0]
            for (i in 1 until getSpanCount()) {
                val nextPosition = staggeredGLM.findLastVisibleItemPositions(null)[i]
                if (nextPosition > position) position = nextPosition
            }
            position
        } else {
            (currentLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        }
    }
}