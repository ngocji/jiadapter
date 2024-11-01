package com.jibase.iflexible.helpers

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import com.jibase.iflexible.adapter.FlexibleAdapter
import com.jibase.iflexible.listener.OnDeleteCompleteListener
import com.jibase.iflexible.listener.OnUndoActionListener
import com.google.android.material.snackbar.Snackbar
import com.jibase.iflexible.utils.Log
import com.jibase.iflexible.view.ISnackBar

class UndoHelper(val adapter: FlexibleAdapter<*>, val callback: OnUndoActionListener) : Snackbar.Callback(), OnDeleteCompleteListener {
    annotation class Action {
        companion object {
            /**
             * Indicates that the Action Listener for confirmation will perform a deletion.
             */
            const val REMOVE = 0
            /**
             * Indicates that the Action Listener for cancellation will perform an update (user responsibility)
             * without removing items.
             */
            const val UPDATE = 1
        }
    }

    companion object {
        const val UNDO_TIMEOUT = 500
        const val TAG = "UndoHelper"
    }

    init {
        adapter.addListener(this)
    }

    private var mAction = Action.REMOVE

    /* Custom style */
    private var mActionTextColor = -1
    private var mBackgroundColor = -1
    private var mMessageTextColor = -1

    private var consecutive = false
    private var mPositions: List<Int>? = null
    private var mPayload: Any? = null
    private var mSnackbar: Snackbar? = null

    /*---------------*/
    /*    CONFIG     */
    /*---------------*/
    /**
     * Sets the payload to inform other linked items about the change in action.
     *
     * @param payload any non-null user object to notify the parent (the payload will be
     *                therefore passed to the bind method of the parent ViewHolder),
     *                pass null to <u>not</u> notify the parent
     * @return this object, so it can be chained
     */
    fun withPayload(payload: Any?): UndoHelper {
        if (payload != null) Log.d("With payload", TAG)
        this.mPayload = payload
        return this
    }

    /**
     * By default [UndoHelper.Action.REMOVE] is performed.
     *
     * @param action the action, one of [UndoHelper.Action.REMOVE], [UndoHelper.Action.UPDATE]
     * @return this object, so it can be chained
     */
    fun withAction(@Action action: Int): UndoHelper {
        this.mAction = action
        return this
    }


    /**
     * Sets the action text color of the action.
     *
     * @param color the color for the action button
     * @return this object, so it can be chained
     */
    fun withActionTextColor(@ColorInt color: Int): UndoHelper {
        Log.d("With customActionTextColor", TAG)
        this.mActionTextColor = color
        return this
    }

    /**
     * Sets the background color of the action.
     *
     * @param color the color for the action button
     * @return this object, so it can be chained
     */
    fun withBackgroundColor(@ColorInt color: Int): UndoHelper {
        Log.d("With customBackgroundColor", TAG)
        this.mBackgroundColor = color
        return this
    }

    /**
     * Sets the text color of the action.
     *
     * @param color the color for the action button
     * @return this object, so it can be chained
     */

    fun withTextColor(@ColorInt color: Int): UndoHelper {
        Log.d("With customTextColor", TAG)
        this.mMessageTextColor = color
        return this
    }

    /**
     * Allows to commit previous action before next consecutive Undo request.
     *
     * Default value is `false` (accumulate items).
     *
     * @param consecutive true to commit deletion at each Undo request to undo last action,
     * false to accumulate the deleted items and Undo all in one shot.
     * @return this object, so it can be chained
     */
    fun withConsecutive(consecutive: Boolean): UndoHelper {
        Log.d("With consecutive=$consecutive", TAG)
        this.consecutive = consecutive
        return this
    }


    /**
     * Performs the action on the specified positions and displays a SnackBar to Undo
     * the operation. To customize the UPDATE event, please set a custom listener with
     * [withAction] method.
     * <p>By default the DELETE action will be performed.</p>
     *
     * @param positions  the position to delete or update
     * @param mainView   the view to find a parent from
     * @param message    the text to show. Can be formatted text
     * @param actionText the action text to display
     * @param duration   How long to display the message. Either {@link Snackbar#LENGTH_SHORT} or
     *                   {@link Snackbar#LENGTH_LONG} or any custom Integer.
     * @return The SnackBar instance
     * @see #start(List, View, int, int, int)
     */

    fun start(positions: List<Int>, targetView: View, message: String, actionText: String, @IntRange(from = -1) duration: Int = -1): Snackbar? {
        this.mPositions = positions
        Log.d("With ${if (mAction == Action.REMOVE) " ACTION REMOVE" else "ACTION UPDATE"}")
        mSnackbar = ISnackBar().of(targetView)
                .setTextColor(mMessageTextColor)
                .setActionColor(mActionTextColor)
                .setBackgroundColor(mBackgroundColor)
                .withMessage(message)
                .withActionName(actionText)
                .withDuration(duration)
                .setAction {
                    callback.onActionCanceled(adapter, mAction, adapter.getUndoPositions())
                    adapter.emptyBin()
                }
                .create()
        mSnackbar?.addCallback(this)
        mSnackbar?.show()
        performAction()
        return mSnackbar
    }

    /**
     * Second function with StringRes
     */
    fun start(positions: List<Int>, targetView: View, @StringRes messageRes: Int, @StringRes actionTextRes: Int, @IntRange(from = -1) duration: Int = -1): Snackbar? {
        val context = targetView.context
        return start(positions, targetView, context.getString(messageRes), context.getString(actionTextRes), duration)
    }


    /*-----------------------*/
    /*    OVERRIDE METHOD  */
    /*---------------------*/

    override fun onDeleteConfirmed(event: Int) {
        callback.onActionConfirm(adapter, mAction, event)
        adapter.confirmDeletion()

        if (true == mSnackbar?.isShown && mAction == Action.REMOVE && !adapter.isRestoreInTime()) {
            mSnackbar?.dismiss()
        }
    }

    @SuppressLint("SwitchIntDef")
    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
        // Check if deletion has already been committed
        // Avoid circular calls!
        if (mAction == Action.REMOVE && !adapter.isRestoreInTime()) {
            return
        }
        when (event) {
            DISMISS_EVENT_SWIPE,
            DISMISS_EVENT_MANUAL,
            DISMISS_EVENT_TIMEOUT -> {
                onDeleteConfirmed(event)
            }
            else -> {
            }
        }
        onDestroy()
    }


    /*-----------------------*/
    /*    PRIVATE METHOD    */
    /*---------------------*/


    private fun onDestroy() {
        adapter.removeListener(this)
        mPositions = null
        mSnackbar = null
        mPayload = null
    }


    private fun performAction() {
        if (consecutive && adapter.isRestoreInTime()) {
            onDeleteConfirmed(DISMISS_EVENT_CONSECUTIVE)
        }

        when (mAction) {
            Action.REMOVE -> {
                adapter.removeItems(mPositions ?: listOf(), mPayload)
            }
            Action.UPDATE -> {
                adapter.saveUndoPositions(mPositions ?: listOf())
            }
        }

        if (adapter.isPermanentDelete()) {
            callback.onActionConfirm(adapter, mAction, DISMISS_EVENT_MANUAL)
        }
    }

}