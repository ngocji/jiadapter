package com.jibase.iflexible.listener

import com.jibase.iflexible.adapter.FlexibleAdapter

interface OnUndoActionListener {
    /**
     * Called when Undo event is triggered. Perform custom action after restoration.
     * Usually for a delete restoration you should call
     * [FlexibleAdapter.restoreDeletedItems].
     *
     * @param action one of [UndoHelper.Action.REMOVE], [UndoHelper.Action.UPDATE]
     * @param positions positions affected
     */
    fun onActionCanceled(adapter: FlexibleAdapter<*>, action: Int, positions: List<Int>)

    /**
     * Called when Undo timeout is over and action must be committed in the user Database.
     * <p>Due to Java Generic, it's too complicated and not well manageable if we pass the
     * {@code List<T>} object.
     * So, to get deleted items, use [FlexibleAdapter.getDeletedItems] from the
     * implementation of this method.
     *
     * @param action one of [UndoHelper.Action.REMOVE], [UndoHelper.Action.UPDATE]
     * @param event  one of [Snackbar.Callback.DISMISS_EVENT_SWIPE],
     *               [Snackbar.Callback.DISMISS_EVENT_MANUAL],
     *               [Snackbar.Callback.DISMISS_EVENT_TIMEOUT],
     *               [Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE]
     */
    fun onActionConfirm(adapter: FlexibleAdapter<*>, action: Int, event: Int)
}