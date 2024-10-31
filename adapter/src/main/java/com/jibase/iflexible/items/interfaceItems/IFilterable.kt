package com.jibase.iflexible.items.interfaceItems

interface IFilterable {
    fun filter(constraint: String): Boolean
}