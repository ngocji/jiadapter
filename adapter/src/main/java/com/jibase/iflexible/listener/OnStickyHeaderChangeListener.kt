package com.jibase.iflexible.listener

import com.jibase.iflexible.adapter.FlexibleAdapter

interface OnStickyHeaderChangeListener {
    fun onStickyHeaderChange(adapter: FlexibleAdapter<*>, newPosition: Int, oldPosition: Int)
}