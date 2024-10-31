package com.jibase.iflexible.viewholder

import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import androidx.annotation.CallSuper
import com.jibase.iflexible.adapter.FlexibleAdapter

abstract class FlexibleExpandableViewHolder(view: View, adapter: FlexibleAdapter<*>, isStickyHeader: Boolean = false) : FlexibleViewHolder(view, adapter, isStickyHeader) {

    /*--------------*/
    /* MAIN METHODS */
    /*--------------*/

    /**
     * Allows to expand or collapse child views of this itemView when [OnClickListener]
     * event occurs on the entire view.
     *
     * This method returns always true; Extend with "return false" to Not expand or collapse
     * this ItemView onClick events.
     *
     * @return always true, if not overridden
     */
    open fun isViewExpandableOnClick(): Boolean {
        return true
    }


    /**
     * Allows to collapse child views of this ItemView when [OnClickListener]
     * event occurs on the entire view.
     *
     * This method returns always true; Extend with "return false" to Not collapse this
     * ItemView onClick events.
     *
     * @return always true, if not overridden
     */
    open fun isViewCollapsibleOnClick(): Boolean {
        return true
    }

    /**
     * Allows to collapse child views of this ItemView when [OnLongClickListener]
     * event occurs on the entire view.
     *
     * This method returns always true; Extend with "return false" to Not collapse this
     * ItemView onLongClick events.
     *
     * @return always true, if not overridden
     */
    open fun isViewCollapsibleOnLongClick(): Boolean {
        return true
    }

    /**
     * Allows to notify change and rebound this itemView on expanding and collapsing events,
     * in order to update the content (so, user can decide to display the current expanding status).
     *
     * This method returns always false; Override with `"return true"` to trigger the
     * notification.
     *
     * @return true to rebound the content of this itemView on expanding and collapsing events,
     * false to ignore the events
     * @see .expandView
     * @see .collapseView
     */
    open fun shouldNotifyParentOnClick(): Boolean {
        return false
    }

    /**
     * Expands or Collapses based on the current state AND on the configuration of the methods
     * [.isViewExpandableOnClick] and [.isViewCollapsibleOnClick].
     *
     * @see .shouldNotifyParentOnClick
     * @see .isViewExpandableOnClick
     * @see .isViewCollapsibleOnClick
     * @see .expandView
     * @see .collapseView
     */
    open fun toggleExpansion() {
        val position = getFlexibleAdapterPosition()
        if (isViewCollapsibleOnClick() && adapter.isExpanded(position)) {
            collapseView(position)
        } else if (isViewExpandableOnClick() && !adapter.isSelected(position)) {
            expandView(position)
        }
    }

    /**
     * Triggers expansion of this itemView.
     *
     * If [.shouldNotifyParentOnClick] returns `true`, this view is rebound
     * with payload [Payload.EXPANDED].
     *
     * @see .shouldNotifyParentOnClick
     */
    open fun expandView(position: Int) {
        adapter.expand(position, notifyParent = shouldNotifyParentOnClick())
    }

    /**
     * Triggers collapse of this itemView.
     *
     * If [.shouldNotifyParentOnClick] returns `true`, this view is rebound
     * with payload [Payload.COLLAPSED].
     *
     * @see .shouldNotifyParentOnClick
     */
    open fun collapseView(position: Int) {
        adapter.collapse(position, shouldNotifyParentOnClick())
        // #320 - Sticky header is not shown correctly once collapsed
        // Scroll to this position if this Expandable is currently sticky
        if (itemView.x < 0 || itemView.y < 0) {
            adapter.getRecyclerView().scrollToPosition(position)
        }
    }

    /*---------------------------------*/
    /* CUSTOM LISTENERS IMPLEMENTATION */
    /*---------------------------------*/

    /**
     * Called when user taps once on the ItemView.
     *
     * **Note:** In Expandable version, it tries to expand, but before,
     * it checks if the view [.isViewExpandableOnClick].
     *
     * @param view the view that receives the event
     */
    @CallSuper
    override fun onClick(v: View) {
        if (adapter.isItemEnabled(getFlexibleAdapterPosition())) {
            toggleExpansion()
        }
        super.onClick(v)
    }

    /**
     * Called when user long taps on this itemView.
     *
     * **Note:** In Expandable version, it tries to collapse, but before,
     * it checks if the view [.isViewCollapsibleOnLongClick].
     *
     * @param view the view that receives the event
     * @since 5.0.0-b1
     */
    @CallSuper
    override fun onLongClick(v: View): Boolean {
        val position = getFlexibleAdapterPosition()
        if (adapter.isItemEnabled(position) && isViewCollapsibleOnLongClick()) {
            collapseView(position)
        }
        return super.onLongClick(v)
    }

    /**
     * {@inheritDoc}
     *
     * **Note:** In the Expandable version, expanded items are forced to collapse.
     *
     * @since 5.0.0-b1
     */
    @CallSuper
    override fun onActionStateChanged(position: Int, actionState: Int) {
        if (adapter.isExpanded(getFlexibleAdapterPosition())) {
            collapseView(position)
        }
        super.onActionStateChanged(position, actionState)
    }
}