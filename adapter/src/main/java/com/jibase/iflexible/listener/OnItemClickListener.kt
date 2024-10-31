package com.jibase.iflexible.listener

import android.view.View
import com.jibase.iflexible.adapter.FlexibleAdapter
import com.jibase.iflexible.items.interfaceItems.IFlexible

interface OnItemClickListener {
    fun onItemClick(adapter: FlexibleAdapter<*>, view: View, position: Int): Boolean
}