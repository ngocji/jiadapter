package com.jibase.iflexible.items.interfaceItems

import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.jibase.iflexible.adapter.FlexibleAdapter

interface IFlexible<VH : RecyclerView.ViewHolder> {
    /*---------------*/
    /* BASIC METHODS */
    /*---------------*/

    /**
     * Returns if the Item is enabled.
     *
     * @return (default) true for enabled item, false for disabled one.
     */
    fun isEnabled(): Boolean

    /**
     * Setter to change enabled behaviour.
     *
     * @param enabled false to disable all operations on this item
     */
    fun setEnabled(enabled: Boolean)

    /**
     * (Internal usage).
     * When and item has been deleted (with Undo) or has been filtered out by the
     * adapter, then, it has hidden status.
     *
     * @return true for hidden item, (default) false for the shown one.
     */
    fun isHidden(): Boolean

    /**
     * Setter to change hidden behaviour. Useful while filtering this item.
     * Default value is false.
     *
     * @param hidden true if this item should remain hidden, false otherwise
     */
    fun setHidden(hidden: Boolean)

    /**
     * Individual item's span size to use only with `GridLayoutManager`.
     *
     * **Note:**
     *
     *  * Default implementation in [AbstractFlexibleItem] already returns 1.
     *  * Not used when `StaggeredGridLayoutManager` is set. With such layout use
     * [eu.davidea.viewholders.FlexibleViewHolder.setFullSpan].
     *
     *
     * @param spanCount current column count
     * @param position  the adapter position of the item
     * @return the number of span occupied by the item at position.
     * @since 5.0.0-rc2
     */
    @IntRange(from = 1)
    fun getSpanSize(spanCount: Int, position: Int): Int

    /**
     * Called by the FlexibleAdapter when it wants to check if this item should be bound
     * again with new content.
     *
     * You should return `true` whether you want this item will be updated because
     * its visual representations will change.
     * **Note: **This method won't be called if
     * [FlexibleAdapter.setNotifyChangeOfUnfilteredItems] is disabled.
     *
     * Default value is `true`.
     *
     * @param newItem The new item object with the new content
     * @return True will trigger a new binding to display new content, false if the content shown
     * is already the latest data.
     */
    fun shouldNotifyChange(newItem: IFlexible<*>): Boolean

    fun getIdView(): String = "${hashCode()}"

    /*--------------------*/
    /* SELECTABLE METHODS */
    /*--------------------*/

    /**
     * Checks if the item can be selected.
     *
     * @return (default) true for a Selectable item, false otherwise
     */
    fun isSelectable(): Boolean

    /**
     * Setter to change enableSelect behaviour.
     *
     * @param selectable false to disable selection on this item
     */
    fun setSelectable(selectable: Boolean)

    /**
     * Custom bubble text for FastScroller.
     *
     * To be called from the implementation of `onCreateBubbleText()`. Example:
     * public String onCreateBubbleText(int position) {
     * return getItem(position).getBubbleText(position);
     * }
     *
     * @param position the current mapped position
     * @return Any desired value
     */
    fun getBubbleText(position: Int): String

    /*-------------------*/
    /* TOUCHABLE METHODS */
    /*-------------------*/

    fun isDraggable(): Boolean

    fun setDraggable(draggable: Boolean)

    fun isSwipeable(): Boolean

    fun setSwipeable(swipeable: Boolean)

    /*---------------------*/
    /* VIEW HOLDER METHODS */
    /*---------------------*/

    /**
     * Identifies a specific view type for this item, used by FlexibleAdapter to auto-map
     * the ViewTypes.
     *
     * **HELP:** To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter [Wiki Page](https://github.com/davideas/FlexibleAdapter/wiki)
     * on GitHub.
     *
     * @return user defined item view type identifier or layout reference if not overridden
     */
    fun getItemViewType(): Int

    /**
     * Delegates the creation of the ViewHolder to the user (AutoMap).
     *
     * **HELP:** To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter [Wiki Page](https://github.com/davideas/FlexibleAdapter/wiki)
     * on GitHub.
     *
     * @param parent   the parent viewGroup
     * @param adapter the Adapter instance extending [FlexibleAdapter]
     * @return a new ViewHolder that holds a View of the given view type
     */
    fun createViewHolder(parent: ViewGroup, adapter: FlexibleAdapter<*>): VH

    /**
     * Delegates the binding of this item's data to the given Layout.
     *
     * **HELP:** To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter [Wiki Page](https://github.com/davideas/FlexibleAdapter/wiki)
     * on GitHub.
     * How to use Payload, please refer to
     * [RecyclerView.Adapter.onBindViewHolder].
     *
     * @param adapter  the FlexibleAdapter instance
     * @param holder   the ViewHolder instance
     * @param position the current position
     * @param payloads a non-null list of merged payloads. Can be empty list if requires full update
     */
    fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: RecyclerView.ViewHolder, position: Int, payloads: List<*>)

    /**
     * Called when a view created by this adapter has been recycled.
     *
     * A view is recycled when a `RecyclerView.LayoutManager` decides that it no longer
     * needs to be attached to its parent RecyclerView. This can be because it has fallen out
     * of visibility or a set of cached views represented by views still attached to the parent
     * RecyclerView.
     * If an item view has large or expensive data bound to it such as large bitmaps, this may be
     * a good place to release those resources.
     *
     * @param adapter  the FlexibleAdapter instance
     * @param holder   the ViewHolder instance being recycled
     * @param position the current position
     */
    fun unbindViewHolder(adapter: FlexibleAdapter<*>, holder: RecyclerView.ViewHolder, position: Int) {}

    /**
     * Called when a view created by this adapter has been attached to a window.
     *
     * This can be used as a reasonable signal that the view is about to be seen by the user.
     *
     * @param adapter  the FlexibleAdapter instance
     * @param holder   the ViewHolder instance being recycled
     * @param position the current position
     */
    fun onViewAttached(adapter: FlexibleAdapter<*>, holder: RecyclerView.ViewHolder, position: Int) {}

    /**
     * Called when a view created by this adapter has been detached from its window.
     *
     * Becoming detached from the window is not necessarily a permanent condition; the consumer of an
     * Adapter's views may choose to cache views offscreen while they are not visible, attaching and
     * detaching them as appropriate.
     *
     * @param adapter  the FlexibleAdapter instance
     * @param holder   the ViewHolder instance being recycled
     * @param position the current position
     */
    fun onViewDetached(adapter: FlexibleAdapter<*>, holder: RecyclerView.ViewHolder, position: Int) {}

}