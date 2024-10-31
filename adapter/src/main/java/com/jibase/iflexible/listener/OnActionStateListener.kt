package com.jibase.iflexible.listener

import androidx.recyclerview.widget.RecyclerView
import com.jibase.iflexible.adapter.FlexibleAdapter

interface OnActionStateListener{
     fun onActionStateChanged(adapter: FlexibleAdapter<*>, viewHolder: RecyclerView.ViewHolder?, actionState: Int)
}