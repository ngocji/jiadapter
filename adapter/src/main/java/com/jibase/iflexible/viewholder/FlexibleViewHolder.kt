package com.jibase.iflexible.viewholder

import android.animation.Animator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jibase.iflexible.helpers.ItemTouchHelperCallback
import com.jibase.iflexible.adapter.FlexibleAdapter
import com.jibase.iflexible.adapter.FlexibleAdapter.Companion.MULTI
import com.jibase.iflexible.utils.LayoutUtils
import com.jibase.iflexible.utils.Log

@Suppress("unused")
abstract class FlexibleViewHolder(preItemView: View, adapter: FlexibleAdapter<*>, isStickyHeader: Boolean = false) : AbstractContentViewHolder(preItemView, adapter, isStickyHeader), ItemTouchHelperCallback.ViewHolderCallback, View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
    // These 2 fields avoid double tactile feedback triggered by Android during the touch event
    // (Drag or Swipe), also assure the LongClick event is correctly fired for ActionMode if that
    // was the user intention.
    private var isLongClickSkipped = false
    private var isAlreadySelected = false

    // State for Dragging & Swiping actions
    open var mActionState = ItemTouchHelper.ACTION_STATE_IDLE

    init {
        if (adapter.onItemClickListener != null) contentView.setOnClickListener(this)
        if (adapter.onItemClickListener != null) contentView.setOnLongClickListener(this)
    }

    override fun onLongClick(v: View): Boolean {
        val position = getFlexibleAdapterPosition()
        if (!adapter.isItemEnabled(position)) return false
        // If LongPressDrag is enabled, then LongClick must be skipped and the listener will
        // be called in onActionStateChanged in Drag mode.
        if (adapter.onItemLongClickListener != null && !adapter.isLongPressDragEnabled()) {
            Log.d("onClick on position $position mode=  ${LayoutUtils.getModeName(adapter.mode)}")
            adapter.onItemLongClickListener?.onItemLongClick(adapter, position)
            toggleActivation()
            return true
        }
        isLongClickSkipped = true
        return false
    }

    override fun onClick(v: View) {
        val position = getFlexibleAdapterPosition()
        if (!adapter.isItemEnabled(position)) return
        // Experimented that, if LongClick is not consumed, onClick is fired. We skip the
        // call to the listener in this case, which is allowed only in ACTION_STATE_IDLE.
        if (mActionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            Log.d("onClick on position $position mode=${LayoutUtils.getModeName(adapter.mode)}")
            // Get the permission to activate the View from user
            if (true == adapter.onItemClickListener?.onItemClick(adapter, v, position)) {
                // Now toggle the activation
                toggleActivation()
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val position = getFlexibleAdapterPosition()
        if (!adapter.isItemEnabled(position) || !isDraggable()) {
            Log.d("Can't start drag: Item is not enabled or draggable!")
            return false
        }
        Log.d("onTouch with DragHandleView on position=$position mode=${LayoutUtils.getModeName(adapter.mode)}")
        if (MotionEvent.ACTION_DOWN == event?.actionMasked && adapter.isHandleDragEnabled()) {
            //Start Drag!
            adapter.getItemTouchHelper()?.startDrag(this)
        }
        return false
    }

    /**
     * Support for StaggeredGridLayoutManager.
     *
     * @param enabled true to enableItem full span size, false to disable
     */
    open fun setFullSpan(enabled: Boolean) {
        if (itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            (itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = enabled
        }
    }

    /**
     * Sets the inner preItemView which will be used to drag this itemView.
     *
     * @param view handle preItemView
     * @see .onTouch
     */
    @SuppressLint("ClickableViewAccessibility")
    @CallSuper
    open fun setDragHandleView(view: View) {
        view.setOnTouchListener { _, event ->
            val position = getFlexibleAdapterPosition()
            if (!adapter.isItemEnabled(position) || !isDraggable()) {
                Log.d("Can't start drag: Item is not enabled or draggable!")
                return@setOnTouchListener false
            }
            Log.d("onClick on position $position mode= ${LayoutUtils.getModeName(adapter.mode)}")
            if (event.actionMasked == MotionEvent.ACTION_DOWN && adapter.isHandleDragEnabled()) {
                //Start Drag!
                adapter.getItemTouchHelper()?.startDrag(this)
            }
            return@setOnTouchListener false
        }
    }


    /**
     * Allows to change and see the activation status on the itemView and to perform animation
     * on inner views.
     *
     * **Important note!** the selected background is visible if you added
     * `android:background="?attr/selectableItemBackground"` in the item layout <u>AND</u>
     * customized the file `style.xml`.
     * Alternatively, to set a background at runtime, you can use the new `DrawableUtils` from UI extension.
     *
     * **Note:** This method must be called every time we want the activation state visible
     * on the itemView, for instance: after a Click (to add the item to the selection list) or
     * after a LongClick (to activate the ActionMode) or during dragging (to show that we enabled
     * the Drag).
     * If you follow the above instructions, it's not necessary to invalidate this preItemView with
     * `notifyItemChanged`: In this way `bindViewHolder` won't be called and inner
     * views can animate without interruptions, eventually you will see the animation running
     * on those inner views at the same time of selection activation.
     */
    @CallSuper
    open fun toggleActivation() {
        // Only for selectable items
        val position = getFlexibleAdapterPosition()
        if (!adapter.isSelectable(position)) return
        // [De] Activate the view
        val selected = adapter.isSelected(position)
        if (contentView.isActivated && !selected || !contentView.isActivated && selected) {
            contentView.isActivated = selected
            if (adapter.getStickyPosition() == position) adapter.ensureHeaderParent()
            // Apply elevation
            if (contentView.isActivated && getActivationElevation() > 0)
                ViewCompat.setElevation(itemView, getActivationElevation())
            else if (getActivationElevation() > 0)
            //Leave unaltered the default elevation
                ViewCompat.setElevation(itemView, 0f)
        }
    }

    /**
     * Allows to set elevation while the preItemView is activated.
     *
     * Override to return desired value of elevation on this itemView.
     * **Note:** returned value must be in Pixel.
     *
     * @return `0px` (never elevate) if not overridden
     */
    open fun getActivationElevation(): Float {
        return 0f
    }

    /**
     * Allows to activate the itemView when Swipe event occurs.
     *
     * This method returns always false; Override with `"return true"` to Not expand or
     * collapse this itemView onClick events.
     *
     * @return always false, if not overridden
     */
    open fun shouldActivateViewWhileSwiping(): Boolean {
        return false
    }

    /**
     * Allows to activate the itemView when Drag event occurs
     * This method returns always false;
     * @return always false, if not overridden
     */
    open fun shouldActivateViewWhileDragging(): Boolean {
        return true
    }

    /**
     * Allows to add and keep item selection if ActionMode is active.
     *
     * This method returns always false;Override with `"return true"`  to add the item
     * to the ActionMode count.
     *
     * @return always false, if not overridden
     */
    open fun shouldAddSelectionInActionMode(): Boolean {
        return false
    }


    /*-----------*/
    /* ANIMATION */
    /*-----------*/

    /**
     * This method is automatically called by FlexibleAdapter to animate the View while the user
     * actively scrolls the list (forward or backward).
     *
     * Implement your logic for different animators based on position, selection and/or
     * direction.
     * Use can take one of the predefined Animator from package UI class `AnimatorHelper`
     * or create your own [Animator](s), then add it to the list of animators.
     *
     * @param animators NonNull list of animators, which you should add new animators
     * @param position  can be used to differentiate the Animators based on positions
     * @param isForward can be used to separate animation from top/bottom or from left/right scrolling
     */
    open fun scrollAnimators(animators: MutableList<Animator>, position: Int, isForward: Boolean) {
        // Free to implement
    }


    /*--------------------------------*/
    /* TOUCH LISTENERS IMPLEMENTATION */
    /*--------------------------------*/

    /**
     * Here we handle the event of when the `ItemTouchHelper` first registers an item
     * as being moved or swiped.
     *
     * In this implementation, View activation is automatically handled if dragged: The Item
     * will be added to the selection list if not selected yet and mode MULTI is activated.
     *
     * @param position    the position of the item touched
     * @param actionState one of [ItemTouchHelper.ACTION_STATE_SWIPE] or
     * [ItemTouchHelper.ACTION_STATE_DRAG].
     * @see .shouldActivateViewWhileSwiping
     * @see .shouldAddSelectionInActionMode
     */
    @CallSuper
    override fun onActionStateChanged(position: Int, actionState: Int) {
        mActionState = actionState
        isAlreadySelected = adapter.isSelected(position)
        Log.d("onClick on position $position mode=  ${LayoutUtils.getModeName(adapter.mode)} \n${if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) "Swipe(1)" else "Drag(2)"}")

        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                if (!isAlreadySelected) {
                    // Be sure, if MULTI is active, to add this item to the selection list (call listener!)
                    // Also be sure user consumes the long click event if not done in onLongClick.
                    // Drag by LongPress or Drag by handleView
                    if (isLongClickSkipped || adapter.mode == MULTI) {
                        // Next check, allows to initiate the ActionMode and to add selection if configured
                        if ((shouldAddSelectionInActionMode() || adapter.mode != MULTI) &&
                                adapter.onItemLongClickListener != null && adapter.isSelectable(position)) {
                            Log.d("onClick on position  $position mode= ${LayoutUtils.getModeName(adapter.mode)}")
                            adapter.onItemLongClickListener?.onItemLongClick(adapter, position)
                            isAlreadySelected = true // Keep selection on release!
                        }
                    }
                    // If still not selected, be sure current item appears selected for the Drag transition
                    if (!isAlreadySelected) {
                        adapter.toggleSelection(position)
                    }
                }
                // Now toggle the activation, Activate preItemView and make selection visible only if necessary
                if (!contentView.isActivated) {
                    toggleActivation()
                }
            }
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if (!isAlreadySelected){
                    adapter.toggleSelection(position)
                }
                if (shouldActivateViewWhileSwiping()){
                    toggleActivation()
                }
//                if (!isAlreadySelected && shouldActivateViewWhileSwiping()) {
//                    adapter.toggleSelection(position)
//                    toggleActivation()
//                }
            }
        }
    }


    /**
     * Here we handle the event of when the ItemTouchHelper has completed the move or swipe.
     *
     * In this implementation, View activation is automatically handled.
     * In case of Drag, the state will be cleared depends by current selection mode!
     *
     * @param position the position of the item released
     * @see .shouldActivateViewWhileSwiping
     * @see .shouldAddSelectionInActionMode
     */
    @CallSuper
    override fun onItemReleased(position: Int) {
        Log.d("onClick on position %s mode=%s: $position ${LayoutUtils.getModeName(adapter.mode)} \n${if (mActionState == ItemTouchHelper.ACTION_STATE_SWIPE) "Swipe(1)" else "Drag(2)"}")
        // Be sure to keep selection if MULTI and shouldAddSelectionInActionMode is active
        if (!isAlreadySelected) {
            when {
                (shouldAddSelectionInActionMode() && adapter.mode == MULTI) -> {
                    Log.d("onClick on position  $position mode= ${LayoutUtils.getModeName(adapter.mode)}")
                    adapter.onItemLongClickListener?.onItemLongClick(adapter, position)
                    if (adapter.isSelected(position)) {
                        toggleActivation()
                    }
                }
                (shouldActivateViewWhileSwiping() && contentView.isActivated) -> {
                    adapter.toggleSelection(position)
                    toggleActivation()
                }
                (mActionState == ItemTouchHelper.ACTION_STATE_DRAG) -> {
                    adapter.toggleSelection(position)
                    if (contentView.isActivated) {
                        toggleActivation()
                    }
                }
            }
        }
        // Reset internal action state ready for next action
        isLongClickSkipped = false
        mActionState = ItemTouchHelper.ACTION_STATE_IDLE
    }


    /*--------------------------------*/
    /* Override ViewHolder CallBack */
    /*--------------------------------*/


    override fun isDraggable(): Boolean {
        return adapter.getItem(getFlexibleAdapterPosition())?.isDraggable() ?: false
    }

    override fun isSwipeable(): Boolean {
        return adapter.getItem(getFlexibleAdapterPosition())?.isSwipeable() ?: false
    }

    override fun getFrontView(): View {
        return itemView
    }

    override fun getRearLeftView(): View? {
        return null
    }

    override fun getRearRightView(): View? {
        return null
    }
}