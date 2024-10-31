package com.jibase.iflexible.items.interfaceItems

import androidx.recyclerview.widget.RecyclerView

interface ISectionable<VH : RecyclerView.ViewHolder, H : IHeader<*>> : IFlexible<VH> {
    fun getHeader(): H?
    fun setHeader(header: IHeader<*>?)
}