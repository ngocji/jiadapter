package com.jibase.iflexible.listener

import com.jibase.iflexible.adapter.FlexibleAdapter

interface OnItemMoveListener : OnActionStateListener{
    /**
     * Called when the item would like to be swapped.
     *
     * Delegate this permission to the user.
     *
     * @param fromPosition the potential start position of the dragged item
     * @param toPosition   the potential resolved position of the swapped item
     * @return return true if the items can swap (`onItemMove()` will be called),
     * false otherwise (nothing happens)
     */
     fun shouldMoveItem(adapter: FlexibleAdapter<*>, fromPosition: Int, toPosition: Int): Boolean

    /**
     * Called when an item has been dragged far enough to trigger a move. **This is called
     * every time an item is shifted**, and **not** at the end of a "drop" event.
     *
     * The end of the "drop" event is instead handled by
     * [FlexibleViewHolder.onItemReleased].
     * **Tip:** Here, you should call [.invalidateItemDecorations] to recalculate
     * item offset if any item decoration has been set.
     *
     * @param fromPosition the start position of the moved item
     * @param toPosition   the resolved position of the moved item
     */
     fun onItemMove(adapter: FlexibleAdapter<*>, fromPosition: Int, toPosition: Int)
}