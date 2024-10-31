package com.jibase.iflexible.listener

import com.jibase.iflexible.adapter.FlexibleAdapter

interface OnItemSwipeListener : OnActionStateListener{
    /**
     * Called when swiping ended its animation and item is not visible anymore.
     *
     * @param position  the position of the item swiped
     * @param direction the direction to which the ViewHolder is swiped, one of:
     *                  {@link ItemTouchHelper#LEFT},
     *                  {@link ItemTouchHelper#RIGHT},
     *                  {@link ItemTouchHelper#UP},
     *                  {@link ItemTouchHelper#DOWN},
     */
     fun onItemSwipe(adapter: FlexibleAdapter<*>, position: Int, direction: Int)
}