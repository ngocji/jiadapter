package com.jibase.iflexible.items.abstractItems

import androidx.recyclerview.widget.RecyclerView
import com.jibase.iflexible.items.interfaceItems.IFlexible

abstract class AbstractFlexibleItem<VH : RecyclerView.ViewHolder> : IFlexible<VH> {
    /* Item flags recognized by the FlexibleAdapter */
    open var enableItem = true
    open var enableHidden = false
    open var enableSelect = false
    open var enableDrag = false
    open var enableSwipe = false

    /*---------------*/
    /* BASIC METHODS */
    /*---------------*/

    /**
     * You **MUST** implement this method to compare items **unique** identifiers.
     *
     * Adapter needs this method to distinguish them and pick up correct items.
     * See [
 * Writing a correct `equals` method](http://developer.android.com/reference/java/lang/Object.html#equals(java.lang.Object)) to implement your own `equals` method.
     *
     * **Hint:** If you don't use unique IDs, reimplement with Basic Java implementation:
     * public boolean equals(Object o) {
     * return this == o;
     *
     * **Important Note:** When used with `Hash[Map,Set]`, the general contract for the
     * `equals` and [.hashCode] methods is that if `equals` returns `true`
     * for any two objects, then `hashCode()` must return the same value for these objects.
     * This means that subclasses of `Object` usually override either both methods or neither
     * of them.
     *
     * @param o instance to compare
     * @return true if items are equals, false otherwise.
     */
    override fun isEnabled(): Boolean {
        return enableItem
    }

    override fun setEnabled(enabled: Boolean) {
        enableItem = enabled
    }

    override fun isHidden(): Boolean {
        return enableHidden
    }

    override fun setHidden(hidden: Boolean) {
        enableHidden = hidden
    }

    override fun getSpanSize(spanCount: Int, position: Int): Int {
        return 1
    }

    override fun shouldNotifyChange(newItem: IFlexible<*>): Boolean {
        return true
    }

    /*--------------------*/
    /* SELECTABLE METHODS */
    /*--------------------*/

    override fun isSelectable(): Boolean {
        return enableSelect
    }

    override fun setSelectable(selectable: Boolean) {
        this.enableSelect = selectable
    }

    override fun getBubbleText(position: Int): String {
        return (position + 1).toString()
    }

    /*-------------------*/
    /* TOUCHABLE METHODS */
    /*-------------------*/

    override fun isDraggable(): Boolean {
        return enableDrag
    }

    override fun setDraggable(draggable: Boolean) {
        enableDrag = draggable
    }

    override fun isSwipeable(): Boolean {
        return enableSwipe
    }

    override fun setSwipeable(swipeable: Boolean) {
        enableSwipe = swipeable
    }

    /*---------------------*/
    /* VIEW HOLDER METHODS */
    /*---------------------*/

    /**
     *
     * If not overridden return value is the same of [.getLayoutRes].
     */
    override fun getItemViewType(): Int {
        return this::class.java.name.hashCode()
    }
}