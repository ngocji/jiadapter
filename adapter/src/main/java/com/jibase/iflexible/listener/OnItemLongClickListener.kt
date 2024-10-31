package com.jibase.iflexible.listener

import com.jibase.iflexible.adapter.FlexibleAdapter

interface OnItemLongClickListener {
    fun onItemLongClick(adapter: FlexibleAdapter<*>, position: Int)
}