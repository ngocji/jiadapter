package com.jibase.iflexible.items.interfaceItems

import com.jibase.iflexible.viewholder.FlexibleExpandableViewHolder

interface IExpandable<VH : FlexibleExpandableViewHolder, S : IFlexible<*>> : IFlexible<VH> {
    /*--------------------*/
    /* EXPANDABLE METHODS */
    /*--------------------*/

    fun isExpanded(): Boolean

    fun setExpanded(expanded: Boolean)

    /**
     * Establish the level of the expansion of this type of item in case of multi level expansion.
     *
     * Default value of first level should return 0.
     * Sub expandable items should return a level +1 for each sub level.
     *
     * @return the level of the expansion of this type of item
     */
    fun getExpansionLevel(): Int

    /*-------------------*/
    /* SUB ITEMS METHODS */
    /*-------------------*/

    fun getSubItems(): List<S>
}