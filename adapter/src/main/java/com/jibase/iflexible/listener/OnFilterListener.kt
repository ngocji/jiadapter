package com.jibase.iflexible.listener

import com.jibase.iflexible.adapter.FlexibleAdapter

interface OnFilterListener{
     fun onUpdateFilterView(adapter: FlexibleAdapter<*>, size: Int)
}