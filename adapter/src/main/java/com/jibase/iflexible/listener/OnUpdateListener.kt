package com.jibase.iflexible.listener

import com.jibase.iflexible.adapter.FlexibleAdapter

interface OnUpdateListener{
     fun onUpdateEmptyView(adapter: FlexibleAdapter<*>, size: Int)
}