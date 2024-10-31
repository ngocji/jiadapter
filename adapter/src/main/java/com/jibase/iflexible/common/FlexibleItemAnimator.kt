package com.jibase.iflexible.common

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jibase.iflexible.viewholder.AnimatedViewHolder
import com.jibase.utils.Log
import java.util.*

open class FlexibleItemAnimator() : SimpleItemAnimator() {
    private val TAG = javaClass.simpleName
    private val mPendingRemovals = mutableListOf<RecyclerView.ViewHolder>()
    private val mPendingAdditions = mutableListOf<RecyclerView.ViewHolder>()
    private val mPendingMoves = mutableListOf<MoveInfo>()
    private val mPendingChanges = mutableListOf<ChangeInfo>()

    private val mAdditionsList = mutableListOf<MutableList<RecyclerView.ViewHolder>>()
    private val mMovesList = mutableListOf<MutableList<MoveInfo>>()
    private val mChangesList = mutableListOf<MutableList<ChangeInfo>>()

    private val mMoveAnimations = mutableListOf<RecyclerView.ViewHolder>()
    private val mChangeAnimations = mutableListOf<RecyclerView.ViewHolder>()
    private val mRemoveAnimations = mutableListOf<RecyclerView.ViewHolder>()
    private val mAddAnimations = mutableListOf<RecyclerView.ViewHolder>()

    private var mInterpolator: Interpolator = LinearInterpolator()

    private var mDefaultInterpolator: TimeInterpolator? = null

    init {
        supportsChangeAnimations = true
    }

    private class MoveInfo(var holder: RecyclerView.ViewHolder,
                           internal val fromX: Int,
                           internal val fromY: Int,
                           internal val toX: Int,
                           internal val toY: Int)

    private class ChangeInfo(var oldHolder: RecyclerView.ViewHolder?,
                             var newHolder: RecyclerView.ViewHolder?,
                             internal val fromX: Int = 0,
                             internal val fromY: Int = 0,
                             internal val toX: Int = 0,
                             internal val toY: Int = 0)

    fun getInterpolator(): Interpolator {
        return this.mInterpolator
    }

    fun setInterpolator(interpolator: Interpolator) {
        this.mInterpolator = interpolator
    }

    override fun runPendingAnimations() {
        val removalsPending = mPendingRemovals.isNotEmpty()
        val movesPending = mPendingMoves.isNotEmpty()
        val changesPending = mPendingChanges.isNotEmpty()
        val additionsPending = mPendingAdditions.isNotEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // Nothing to animate
            return
        }
        // First, remove animations
        runRemoveAnimation()
        // Next, move animations
        runMoveAnimation(removalsPending, movesPending)
        // Next, change animations
        runChangeAnimation(removalsPending, changesPending)
        // Next, add animations
        runAddAnimation(removalsPending, changesPending, movesPending, additionsPending)
    }

    //1st Remove
    private fun runRemoveAnimation() {
        // Reverse sorting removal animations
        mPendingRemovals.sortByDescending { it.itemId }
        val remover = Runnable {
            Log.d("Run remove---> ${mPendingRemovals.size}", TAG)
            mPendingRemovals.forEachIndexed { index, holder ->
                doAnimateRemove(holder, index)
            }
            mPendingRemovals.clear()
        }
        remover.run()
    }

    //2nd Move
    private fun runMoveAnimation(removalsPending: Boolean, movesPending: Boolean) {
        if (movesPending) {
            val moves = mPendingMoves.toMutableList()
            mMovesList.add(moves)
            mPendingMoves.clear()
            val mover = Runnable {
                Log.d("Run move---> ${mMovesList.size}", TAG)
                moves.forEach { moveInfo ->
                    animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY,
                            moveInfo.toX, moveInfo.toY)
                }
                moves.clear()
                mMovesList.remove(moves)
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                ViewCompat.postOnAnimationDelayed(view, mover, removeDuration)
            } else {
                mover.run()
            }
        }
    }

    //3rd Change
    private fun runChangeAnimation(removalsPending: Boolean, changesPending: Boolean) {
        // Change animation to run in parallel with move animations
        if (changesPending) {
            val changes = mPendingChanges.toMutableList()
            mChangesList.add(changes)
            mPendingChanges.clear()
            val changer = Runnable {
                Log.d("Run change---> ${changes.size}", TAG)
                changes.forEach {
                    animateChangeImpl(it)
                }
                changes.clear()
                mChangesList.remove(changes)
            }
            if (removalsPending) {
                changes[0].oldHolder?.itemView?.also {
                    ViewCompat.postOnAnimationDelayed(it, changer, removeDuration)
                }
            } else {
                changer.run()
            }
        }
    }

    //4th Add
    private fun runAddAnimation(removalsPending: Boolean, changesPending: Boolean,
                                movesPending: Boolean, additionsPending: Boolean) {
        if (additionsPending) {
            mPendingAdditions.sortWith(Comparator { vh1, vh2 -> vh1.layoutPosition - vh2.layoutPosition })

            val additions = mPendingAdditions.toMutableList()
            // Sorting addition animations based on it's original layout position

            mAdditionsList.add(additions)
            mPendingAdditions.clear()
            val adder = Runnable {
                Log.d("Run add---> ${mAddAnimations.size}", TAG)
                additions.forEachIndexed { index, holder ->
                    doAnimateAdd(holder, index)
                }
                additions.clear()
                mAdditionsList.remove(additions)
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = if (removalsPending) removeDuration else 0
                val moveDuration = if (movesPending) moveDuration else 0
                val changeDuration = if (changesPending) changeDuration else 0
                val totalDelay = removeDuration + Math.max(moveDuration, changeDuration)
                val view = additions[0].itemView
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
            } else {
                adder.run()
            }
        }
    }


    /* ====== */
    /* REMOVE */
    /* ====== */

    /**
     * Prepares the View for Remove Animation.
     *
     * - If [AnimatedViewHolder.preAnimateRemoveImpl] is implemented and returns
     * `true`, then ViewHolder has precedence and the implementation of this method is ignored;
     * - If not, the implementation of this method is therefore performed.
     * Default value is `true`.
     *
     * @param holder the ViewHolder
     * @return `true` if a later call to [runPendingAnimations] is requested,
     * false otherwise.
     */
    open fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    /**
     * Performs the Remove Animation of this ViewHolder.
     *
     * - If [AnimatedViewHolder.animateRemoveImpl] is
     * implemented and returns true, then ViewHolder has precedence and the implementation of this
     * method is ignored;
     * - If not, the implementation of this method is therefore performed.
     *
     * @param holder the ViewHolder
     * @param index  the progressive order of execution
     */
    open fun animateRemoveImpl(holder: RecyclerView.ViewHolder, index: Int) {
        //Free to implement
    }

    private fun preAnimateRemove(holder: RecyclerView.ViewHolder): Boolean {
        clear(holder.itemView)
        var consumed = false
        if (holder is AnimatedViewHolder) {
            consumed = (holder as AnimatedViewHolder).preAnimateRemoveImpl()
        }
        return consumed || preAnimateRemoveImpl(holder)
    }

    private fun doAnimateRemove(holder: RecyclerView.ViewHolder, index: Int) {
        Log.d("AnimateRemove on itemId=${holder.itemId}  layoutPosition=${holder.layoutPosition}", TAG)
        var consumed = false
        if (holder is AnimatedViewHolder) {
            consumed = (holder as AnimatedViewHolder).animateRemoveImpl(DefaultRemoveVpaListener(holder), removeDuration, index)
        }
        if (!consumed) {
            animateRemoveImpl(holder, index)
        }
        mRemoveAnimations.add(holder)
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        Log.d("Animate remove ---> $holder")
        endAnimation(holder)
        return preAnimateRemove(holder) && mPendingRemovals.add(holder)
    }

    /* === */
    /* ADD */
    /* === */

    /**
     * Prepares the View for Add Animation.
     *
     * - If [AnimatedViewHolder.preAnimateAddImpl] is implemented and returns
     * `true`, then ViewHolder has precedence and the implementation of this method is ignored;
     * - If not, the implementation of this method is therefore performed.
     * Default value is `true`.
     *
     * @param holder the ViewHolder
     * @return `true` if a later call to [.runPendingAnimations] is requested,
     * false otherwise.
     */
    open fun preAnimateAddImpl(holder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    /**
     * Performs the Add Animation of this ViewHolder.
     *
     * - If [AnimatedViewHolder.animateAddImpl] is
     * implemented and returns `true`, then ViewHolder has precedence and the implementation
     * of this method is ignored;
     * - If not, the implementation of this method is therefore performed.
     *
     * @param holder the ViewHolder
     * @param index  the progressive order of execution
     */
    open fun animateAddImpl(holder: RecyclerView.ViewHolder, index: Int) {
        //Free to implement
    }

    private fun preAnimateAdd(holder: RecyclerView.ViewHolder): Boolean {
        clear(holder.itemView)
        var consumed = false
        if (holder is AnimatedViewHolder) {
            consumed = (holder as AnimatedViewHolder).preAnimateAddImpl()
        }
        return consumed || preAnimateAddImpl(holder)
    }

    private fun doAnimateAdd(holder: RecyclerView.ViewHolder, index: Int) {
        Log.d("AnimateAdd on itemId=${holder.itemId} position=${holder.layoutPosition}", TAG)
        var consumed = false
        if (holder is AnimatedViewHolder) {
            consumed = (holder as AnimatedViewHolder).animateAddImpl(DefaultAddVpaListener(holder), addDuration, index)
        }
        if (!consumed) {
            animateAddImpl(holder, index)
        }
        mAddAnimations.add(holder)
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        endAnimation(holder)
        return preAnimateAdd(holder) && mPendingAdditions.add(holder)
    }

    /* ==== */
    /* MOVE */
    /* ==== */

    override fun animateMove(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        var nfromX = fromX
        var nfromY = fromY
        val view = holder.itemView
        nfromX += holder.itemView.translationX.toInt()
        nfromY += holder.itemView.translationY.toInt()
        resetAnimation(holder)
        val deltaX = toX - nfromX
        val deltaY = toY - nfromY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (deltaX != 0) {
            view.translationX = (-deltaX).toFloat()
        }
        if (deltaY != 0) {
            view.translationY = (-deltaY).toFloat()
        }
        mPendingMoves.add(MoveInfo(holder, nfromX, nfromY, toX, toY))
        return true
    }

    private fun animateMoveImpl(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0f)
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0f)
        }
        // TODO: make EndActions end listeners instead, since end actions aren't called when
        // VPAs are canceled (and can't end them. why?)
        // Need listener functionality in VPACompat for this. Ick.
        mMoveAnimations.add(holder)
        val animation = ViewCompat.animate(view)
        animation.setDuration(moveDuration).setListener(object : VpaListenerAdapter {
            override fun onAnimationStart(view: View) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(view: View) {
                if (deltaX != 0) {
                    view.translationX = 0f
                }
                if (deltaY != 0) {
                    view.translationY = 0f
                }
            }

            override fun onAnimationEnd(view: View) {
                animation.setListener(null)
                dispatchMoveFinished(holder)
                mMoveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }


    /* ====== */
    /* CHANGE */
    /* ====== */

    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder?,
                               fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        if (oldHolder === newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY)
        }
        val prevTranslationX = oldHolder.itemView.translationX
        val prevTranslationY = oldHolder.itemView.translationY
        val prevAlpha = oldHolder.itemView.alpha
        resetAnimation(oldHolder)
        val deltaX = (toX.toFloat() - fromX.toFloat() - prevTranslationX).toInt()
        val deltaY = (toY.toFloat() - fromY.toFloat() - prevTranslationY).toInt()
        // Recover prev translation state after ending animation
        oldHolder.itemView.translationX = prevTranslationX
        oldHolder.itemView.translationY = prevTranslationY
        oldHolder.itemView.alpha = prevAlpha
        if (newHolder != null) {
            // Carry over translation values
            resetAnimation(newHolder)
            newHolder.itemView.translationX = (-deltaX).toFloat()
            newHolder.itemView.translationY = (-deltaY).toFloat()
            newHolder.itemView.alpha = 0f
        }
        mPendingChanges.add(ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY))
        return true
    }

    private fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            val oldViewAnim = ViewCompat.animate(view).setDuration(changeDuration)
            changeInfo.oldHolder?.also {
                mChangeAnimations.add(it)
            }
            oldViewAnim.translationX((changeInfo.toX - changeInfo.fromX).toFloat())
            oldViewAnim.translationY((changeInfo.toY - changeInfo.fromY).toFloat())
            oldViewAnim.alpha(0f).setListener(object : VpaListenerAdapter {
                override fun onAnimationStart(view: View) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(view: View) {
                    oldViewAnim.setListener(null)
                    view.alpha = 1f
                    view.translationX = 0f
                    view.translationY = 0f
                    dispatchChangeFinished(changeInfo.oldHolder, true)
                    changeInfo.oldHolder?.also {
                        mChangeAnimations.remove(it)
                    }
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            val newViewAnimation = ViewCompat.animate(newView)
            changeInfo.newHolder?.also {
                mChangeAnimations.add(it)
            }
            newViewAnimation.translationX(0f).translationY(0f).setDuration(changeDuration).alpha(1f).setListener(object : VpaListenerAdapter {
                override fun onAnimationStart(view: View) {
                    dispatchChangeStarting(changeInfo.newHolder, false)
                }

                override fun onAnimationEnd(view: View) {
                    newViewAnimation.setListener(null)
                    newView.alpha = 1f
                    newView.translationX = 0f
                    newView.translationY = 0f
                    dispatchChangeFinished(changeInfo.newHolder, false)
                    changeInfo.newHolder?.also {
                        mChangeAnimations.remove(it)
                    }
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
    }

    private fun endChangeAnimation(infoList: MutableList<ChangeInfo>, item: RecyclerView.ViewHolder) {
        for (i in infoList.indices.reversed()) {
            val changeInfo = infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder)
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo, item: RecyclerView.ViewHolder?): Boolean {
        var oldItem = false
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null
        } else if (changeInfo.oldHolder == item) {
            changeInfo.oldHolder = null
            oldItem = true
        } else {
            return false
        }
        item?.itemView?.apply {
            alpha = 1f
            translationX = 0f
            translationY = 0f
        }
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        val view = item.itemView
        // This will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel()
        // TODO: if some other animations are chained to end, how do we cancel them as well?
        for (i in mPendingMoves.indices.reversed()) {
            val moveInfo = mPendingMoves[i]
            if (moveInfo.holder == item) {
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(item)
                mPendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(mPendingChanges, item)
        if (mPendingRemovals.remove(item)) {
            clear(item.itemView)
            dispatchRemoveFinished(item)
        }
        if (mPendingAdditions.remove(item)) {
            clear(item.itemView)
            dispatchAddFinished(item)
        }

        for (i in mChangesList.indices.reversed()) {
            val changes = mChangesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                mChangesList.removeAt(i)
            }
        }
        for (i in mMovesList.indices.reversed()) {
            val moves = mMovesList[i]
            for (j in moves.indices.reversed()) {
                val moveInfo = moves[j]
                if (moveInfo.holder === item) {
                    view.translationY = 0f
                    view.translationX = 0f
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        mMovesList.removeAt(i)
                    }
                    break
                }
            }
        }
        for (i in mAdditionsList.indices.reversed()) {
            val additions = mAdditionsList[i]
            if (additions.remove(item)) {
                clear(item.itemView)
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    mAdditionsList.removeAt(i)
                }
            }
        }
        // Animations should be ended by the cancel above.
        // Used during DEBUGGING; Commented in final version.
        //		if (mRemoveAnimations.remove(item)) {
        //			throw new IllegalStateException(
        //					"After animation is cancelled, item should not be in mRemoveAnimations list");
        //		}
        //		if (mAddAnimations.remove(item)) {
        //			throw new IllegalStateException(
        //					"After animation is cancelled, item should not be in mAddAnimations list");
        //		}
        //		if (mChangeAnimations.remove(item)) {
        //			throw new IllegalStateException(
        //					"After animation is cancelled, item should not be in mChangeAnimations list");
        //		}
        //		if (mMoveAnimations.remove(item)) {
        //			throw new IllegalStateException(
        //					"After animation is cancelled, item should not be in mMoveAnimations list");
        //		}
        dispatchFinishedWhenDone()
    }

    private fun resetAnimation(holder: RecyclerView.ViewHolder) {
        if (mDefaultInterpolator == null) {
            mDefaultInterpolator = ValueAnimator().interpolator
        }
        // Clear Interpolator
        holder.itemView.animate().interpolator = mDefaultInterpolator
        endAnimation(holder)
    }

    override fun isRunning(): Boolean {
        return !mPendingAdditions.isEmpty() ||
                !mPendingChanges.isEmpty() ||
                !mPendingMoves.isEmpty() ||
                !mPendingRemovals.isEmpty() ||
                !mMoveAnimations.isEmpty() ||
                !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() ||
                !mChangeAnimations.isEmpty() ||
                !mMovesList.isEmpty() ||
                !mAdditionsList.isEmpty() ||
                !mChangesList.isEmpty()
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call [.dispatchAnimationsFinished] to notify any
     * listeners.
     */
    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimations() {
        var count = mPendingMoves.size
        for (i in count - 1 downTo 0) {
            val item = mPendingMoves[i]
            val view = item.holder.itemView
            view.translationY = 0f
            view.translationX = 0f
            dispatchMoveFinished(item.holder)
            mPendingMoves.removeAt(i)
        }
        count = mPendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = mPendingRemovals[i]
            dispatchRemoveFinished(item)
            mPendingRemovals.removeAt(i)
        }
        count = mPendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = mPendingAdditions[i]
            clear(item.itemView)
            dispatchAddFinished(item)
            mPendingAdditions.removeAt(i)
        }
        count = mPendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(mPendingChanges[i])
        }
        mPendingChanges.clear()
        if (!isRunning) {
            return
        }

        var listCount = mMovesList.size
        for (i in listCount - 1 downTo 0) {
            val moves = mMovesList[i]
            count = moves.size
            for (j in count - 1 downTo 0) {
                val moveInfo = moves[j]
                val item = moveInfo.holder
                val view = item.itemView
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(moveInfo.holder)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    mMovesList.remove(moves)
                }
            }
        }
        listCount = mAdditionsList.size
        for (i in listCount - 1 downTo 0) {
            val additions = mAdditionsList[i]
            count = additions.size
            for (j in count - 1 downTo 0) {
                val item = additions[j]
                val view = item.itemView
                view.alpha = 1f
                dispatchAddFinished(item)
                // Prevent exception when removal already occurred during finishing animation
                if (j < additions.size) {
                    additions.removeAt(j)
                }
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions)
                }
            }
        }
        listCount = mChangesList.size
        for (i in listCount - 1 downTo 0) {
            val changes = mChangesList[i]
            count = changes.size
            for (j in count - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    mChangesList.remove(changes)
                }
            }
        }

        cancelAll(mRemoveAnimations)
        cancelAll(mMoveAnimations)
        cancelAll(mAddAnimations)
        cancelAll(mChangeAnimations)

        dispatchAnimationsFinished()
    }

    private fun cancelAll(viewHolders: List<RecyclerView.ViewHolder>) {
        for (i in viewHolders.indices.reversed()) {
            ViewCompat.animate(viewHolders[i].itemView).cancel()
        }
    }

    private fun clear(v: View) {
        v.alpha = 1f
        v.scaleY = 1f
        v.scaleX = 1f
        v.translationY = 0f
        v.translationX = 0f
        v.rotation = 0f
        v.rotationY = 0f
        v.rotationX = 0f
        v.pivotY = (v.measuredHeight / 2).toFloat()
        v.pivotX = (v.measuredWidth / 2).toFloat()
        v.animate().setInterpolator(null).startDelay = 0
    }

    /**
     * {@inheritDoc}
     *
     *
     * If the payload list is not empty, DefaultItemAnimator returns `true`.
     * When this is the case:
     *
     *  * If you override `animateChange()`, both ViewHolder arguments will be the same
     * instance.
     *  * If you are not overriding `animateChange()`, then DefaultItemAnimator will call
     * `animateMove()` and run a move animation instead.
     *
     */
    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder,
                                           payloads: List<Any>): Boolean {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads)
    }

    interface VpaListenerAdapter : ViewPropertyAnimatorListener {

        override fun onAnimationStart(view: View) {}

        override fun onAnimationEnd(view: View) {}

        override fun onAnimationCancel(view: View) {}
    }

    open inner class DefaultAddVpaListener(var mViewHolder: RecyclerView.ViewHolder) : VpaListenerAdapter {

        override fun onAnimationStart(view: View) {
            dispatchAddStarting(mViewHolder)
        }

        override fun onAnimationCancel(view: View) {
            clear(view)
        }

        override fun onAnimationEnd(view: View) {
            clear(view)
            dispatchAddFinished(mViewHolder)
            mAddAnimations.remove(mViewHolder)
            dispatchFinishedWhenDone()
        }
    }

    open inner class DefaultRemoveVpaListener(var mViewHolder: RecyclerView.ViewHolder) : VpaListenerAdapter {

        override fun onAnimationStart(view: View) {
            dispatchRemoveStarting(mViewHolder)
        }

        override fun onAnimationCancel(view: View) {
            clear(view)
        }

        override fun onAnimationEnd(view: View) {
            clear(view)
            dispatchRemoveFinished(mViewHolder)
            mRemoveAnimations.remove(mViewHolder)
            dispatchFinishedWhenDone()
        }
    }

}