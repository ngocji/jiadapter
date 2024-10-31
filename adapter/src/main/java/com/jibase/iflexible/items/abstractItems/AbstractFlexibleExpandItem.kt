package com.jibase.iflexible.items.abstractItems

import com.jibase.extensions.hasPosition
import com.jibase.iflexible.items.interfaceItems.IExpandable
import com.jibase.iflexible.items.interfaceItems.IFlexible
import com.jibase.iflexible.viewholder.FlexibleExpandableViewHolder

abstract class AbstractFlexibleExpandItem<VH : FlexibleExpandableViewHolder, S : IFlexible<*>>(private var subItems: MutableList<S> = mutableListOf()) : AbstractFlexibleItem<VH>(), IExpandable<VH, S> {
    /* Flags for FlexibleAdapter */
    open var isExpand = false

    /*--------------------*/
    /* EXPANDABLE METHODS */
    /*--------------------*/


    override fun isExpanded() = isExpand

    override fun setExpanded(expanded: Boolean) {
        isExpand = expanded
    }

    override fun getExpansionLevel() = 0

    override fun getSubItems(): List<S> = subItems

    /*-------------------*/
    /* SUB ITEMS METHODS */
    /*-------------------*/

    fun hasSubItems(): Boolean {
        return subItems.isNotEmpty()
    }

    fun setSubItems(subItems: List<S>): AbstractFlexibleExpandItem<VH, S> {
        this.subItems = subItems.toMutableList()
        return this
    }

    fun addSubItems(position: Int, subItems: List<S>): AbstractFlexibleExpandItem<VH, S> {
        if (this.subItems hasPosition position) {
            this.subItems.addAll(position, subItems)
        } else {
            this.subItems.addAll(subItems)
        }
        return this
    }

    fun addSubItem(subItem: S): AbstractFlexibleExpandItem<VH, S> {
        subItems.add(subItem)
        return this
    }

    fun addSubItem(position: Int, subItem: S): AbstractFlexibleExpandItem<VH, S> {
        if (this.subItems hasPosition position) {
            subItems.add(position, subItem)
        } else {
            addSubItem(subItem)
        }
        return this
    }

    fun getSubItemsCount(): Int {
        return subItems.size
    }


    fun getSubItem(position: Int): S? {
        return subItems.getOrNull(position)
    }

    fun getSubItemPosition(subItem: S): Int {
        return subItems.indexOf(subItem)
    }

    operator fun contains(subItem: S): Boolean {
        return subItems.contains(subItem)
    }

    fun removeSubItem(item: S?): Boolean {
        return item != null && subItems.remove(item)
    }

    fun removeSubItems(subItems: List<S>?): Boolean {
        return subItems != null && this.subItems.removeAll(subItems)
    }

    fun removeSubItem(position: Int): Boolean {
        if (this.subItems hasPosition position) {
            subItems.removeAt(position)
            return true
        }
        return false
    }
}