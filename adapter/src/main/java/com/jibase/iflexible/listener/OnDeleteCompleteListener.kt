package com.jibase.iflexible.listener

interface OnDeleteCompleteListener{
    /**
     * Called when UndoTime out is over or when Filter is started or reset in order
     * to commit deletion in the user Repository.
     *
     * **Note:** Must be called on user Main Thread!
     *
     * @param event One of the event of `Snackbar.Callback`
     */
    fun onDeleteConfirmed(event: Int)
}