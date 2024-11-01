package com.jibase.iflexible.adapter

import android.animation.Animator
import android.animation.AnimatorSet
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.SparseArray
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.jibase.iflexible.viewholder.FlexibleViewHolder
import com.jibase.iflexible.utils.Log
import java.util.*

abstract class AbstractFlexibleAnimatorAdapter(hasStateId: Boolean) : AbstractFlexibleAdapter() {
    private val TAG = javaClass.simpleName
    private val DEFAULT_DURATION = 300L

    private var interpolator: Interpolator = LinearInterpolator()
    private val animatorNotifierObserver = AnimatorAdapterDataObserver()
    private var entryStep = true

    /**
     * The active Animators. Keys are hash codes of the Views that are animated.
     */
    private val sparseAnimators = SparseArray<Animator>()

    /**
     * The position of the last item that was animated.
     */
    private var lastAnimatedPosition = -1

    /**
     * Max items RecyclerView displays
     */
    private var maxChildViews = -1

    /**
     * Contains type of animators already added
     */
    private var isReverseEnabled = false
    private var isForwardEnabled = false
    private var onlyEntryAnimation = false
    private var animateFromObserver = false

    private var mInitialDelay = 0L
    private var mStepDelay = 100L
    private var mDuration = DEFAULT_DURATION

    init {
        setHasStableIds(hasStateId)
        registerAdapterDataObserver(animatorNotifierObserver)
    }

    /*-----------------------*/
    /* CONFIGURATION SETTERS */
    /*-----------------------*/

    /**
     * @param animate true to notify this Adapter that initialization is started and so
     * animate items, false to inform that the operation is complete
     */
    open fun setScrollAnimate(animate: Boolean): AbstractFlexibleAnimatorAdapter {
        this.animateFromObserver = animate
        return this
    }

    /**
     * Sets the initial delay for the first item animation.
     *
     * Default value is `0ms`.
     *
     * @param initialDelay any non negative delay
     * @return this AnimatorAdapter, so the call can be chained
     */
    open fun setAnimationInitialDelay(initialDelay: Long): AbstractFlexibleAnimatorAdapter {
        Log.d("Set animationInitialDelay=$initialDelay",TAG)
        mInitialDelay = initialDelay
        return this
    }

    /**
     * Sets the step delay between an animation and the next to be added to the initial delay.
     *
     * The delay is added on top of the previous delay.
     * Default value is `100ms`.
     *
     * @param delay any positive delay
     * @return this AnimatorAdapter, so the call can be chained
     */
    open fun setAnimationDelay(@IntRange(from = 0) delay: Long): AbstractFlexibleAnimatorAdapter {
        Log.d("Set animationDelay=$delay", TAG)
        mStepDelay = delay
        return this
    }

    /**
     * If initial loading animation should use step delay between an item animation and the next.
     * When false, all items are animated with no delay.
     *
     * Better to disable when using Grid layouts.
     * Default value is `true`.
     *
     * @param entryStep true to enableItem step delay, false otherwise
     * @return this AnimatorAdapter, so the call can be chained
     */
    open fun setAnimationEntryStep(entryStep: Boolean): AbstractFlexibleAnimatorAdapter {
        Log.d("Set animationEntryStep=$entryStep", TAG)
        this.entryStep = entryStep
        return this
    }

    /**
     * Sets the duration of the animation for ALL items.
     *
     * Default value is `300ms`.
     *
     * @param duration any positive time
     * @return this AnimatorAdapter, so the call can be chained
     */
    open fun setAnimationDuration(@IntRange(from = 1) duration: Long): AbstractFlexibleAnimatorAdapter {
        Log.d("Set animationDuration=$duration", TAG)
        mDuration = duration
        return this
    }

    /**
     * Sets a custom interpolator for ALL items.
     *
     * Default value is [LinearInterpolator].
     *
     * @param interpolator any valid non null interpolator
     * @return this AnimatorAdapter, so the call can be chained
     */
    open fun setAnimationInterpolator(interpolator: Interpolator): AbstractFlexibleAnimatorAdapter {
        Log.d("Set animationInterpolator=$interpolator", TAG)
        this.interpolator = interpolator
        return this
    }

    /**
     * Enables/Disables item animation while forward scrolling and on loading.
     * Enabling forward scrolling will disable `onlyEntryAnimation`.
     * Forward scrolling is independent from reverse scrolling.
     *
     * Default value is `false`.
     * **Note:** Loading animation can only be performed if the Adapter is initialized
     * with some items using the constructor.
     *
     * @param enabled true to enableItem item animation on forward scrolling, false to disable.
     * @return this AnimatorAdapter, so the call can be chained
     * @see setOnlyEntryAnimation
     * @see setAnimationOnReverseScrolling
     */
    open fun setAnimationOnForwardScrolling(enabled: Boolean): AbstractFlexibleAnimatorAdapter {
        Log.d("Set animationOnForwardScrolling=$enabled", TAG)
        if (enabled) this.onlyEntryAnimation = false
        isForwardEnabled = enabled
        return this
    }

    /**
     * @return true if items are animated when forward scrolling, false only forward
     */
    open fun isAnimationOnForwardScrollingEnabled(): Boolean {
        return isForwardEnabled
    }

    /**
     * Enables/Disables reverse scrolling animation.
     * Reverse scrolling is independent from forward scrolling.
     *
     * Default value is `false` (only forward).
     *
     * @param enabled true to enableItem item animation on reverse scrolling, false to disable.
     * @return this AnimatorAdapter, so the call can be chained
     * @see setOnlyEntryAnimation
     * @see setAnimationOnForwardScrolling
     */
    open fun setAnimationOnReverseScrolling(enabled: Boolean): AbstractFlexibleAnimatorAdapter {
        Log.d("Set animationOnReverseScrolling=$enabled", TAG)
        isReverseEnabled = enabled
        return this
    }

    /**
     * @return true if items are animated when reverse scrolling, false only forward
     */
    open fun isAnimationOnReverseScrollingEnabled(): Boolean {
        return isReverseEnabled
    }

    /**
     * Performs only entry animation during the initial loading. Stops the animation after
     * the last visible item in the RecyclerView has been animated.
     *
     * **Note:** Loading animation can only be performed if the Adapter is initialized
     * with some items using the Constructor.
     * Default value is `false`.
     *
     * @param enabled true to perform only entry animation, false otherwise
     * @return this AnimatorAdapter, so the call can be chained
     * @see setAnimationOnForwardScrolling
     */
    open fun setOnlyEntryAnimation(enabled: Boolean): AbstractFlexibleAnimatorAdapter {
        Log.d("Set onlyEntryAnimation=$enabled", TAG)
        if (enabled) this.isForwardEnabled = true
        this.onlyEntryAnimation = enabled
        return this
    }

    /**
     * @return true if the scrolling animation will occur only at startup until the screen is
     * filled with the items, false animation will be performed when scrolling too.
     */
    open fun isOnlyEntryAnimation(): Boolean {
        return onlyEntryAnimation
    }

    /*--------------*/
    /* MAIN METHODS */
    /*--------------*/

    /**
     * Cancels any existing animations for given View. Useful when fling.
     */
    private fun cancelExistingAnimation(hashCode: Int) {
        sparseAnimators.get(hashCode)?.end()
    }

    /**
     * Checks if at the provided position, the item is a Header or Footer.
     *
     * @param position the position to check
     * @return true if it's a scrollable item
     */
    abstract fun isScrollableHeaderOrFooter(position: Int): Boolean

    /**
     * Performs checks to scroll animate the itemView and in case, it animates the view.
     *
     * **Note:** If you have to change at runtime the LayoutManager *and* add
     * Scrollable Headers too, consider to add them in post, using a `delay >= 0`,
     * otherwise scroll animations on all items will not start correctly.
     *
     * @param holder   the ViewHolder just bound
     * @param position the current item position
     */
    protected fun animateView(holder: RecyclerView.ViewHolder, position: Int) {
        // Use always the max child count reached
        if (maxChildViews < mRecyclerView.childCount) {
            maxChildViews = mRecyclerView.childCount
        }
        // Animate only during initial loading?
        if (onlyEntryAnimation && lastAnimatedPosition >= maxChildViews) {
            isForwardEnabled = false
        }
        val lastVisiblePosition = getFlexibleLayoutManager().findLastVisibleItemPosition()

        if ((isForwardEnabled || isReverseEnabled)
                && !isFastScroll && holder is FlexibleViewHolder
                && (!animatorNotifierObserver.isPositionNotified || isScrollableHeaderOrFooter(position))
                && (isScrollableHeaderOrFooter(position)
                        || isForwardEnabled && position > lastVisiblePosition
                        || isReverseEnabled && position < lastVisiblePosition
                        || position == 0 && maxChildViews == 0)) {

            // Cancel animation is necessary when fling
            val hashCode = holder.itemView.hashCode()
            cancelExistingAnimation(hashCode)

            // User animators
            val animators = mutableListOf<Animator>()
            holder.scrollAnimators(animators, position, position >= lastVisiblePosition)
            if (animators.isNotEmpty()) {
                // Execute the animations together
                val set = AnimatorSet()
                set.playTogether(animators)
                set.interpolator = interpolator
                // Single view duration
                var duration = mDuration
                for (animator in animators) {
                    if (animator.duration != DEFAULT_DURATION) {
                        duration = animator.duration
                    }
                }
                set.duration = duration
                set.addListener(HelperAnimatorListener(hashCode))
                if (entryStep) {
                    // Stop stepDelay when screen is filled
                    set.startDelay = calculateAnimationDelay(holder, position)
                }
                set.start()
                sparseAnimators.put(hashCode, set)
            }
        }
        animatorNotifierObserver.clearNotified()
        // Update last animated position
        lastAnimatedPosition = position
    }

    /**
     * @param position the position just bound
     * @return the delay in milliseconds after which, the animation for next ItemView should start.
     */
    private fun calculateAnimationDelay(holder: RecyclerView.ViewHolder, position: Int): Long {
        var delay: Long
        var firstVisiblePosition = getFlexibleLayoutManager().findFirstCompletelyVisibleItemPosition()
        var lastVisiblePosition = getFlexibleLayoutManager().findLastCompletelyVisibleItemPosition()

        // Fix for high delay on the first visible item on rotation
        if (firstVisiblePosition < 0 && position >= 0)
            firstVisiblePosition = position - 1

        // Last visible position is the last animated when initially loading
        if (position - 1 > lastVisiblePosition)
            lastVisiblePosition = position - 1

        val visibleItems = lastVisiblePosition - firstVisiblePosition
        val numberOfAnimatedItems = position - 1

        // Normal Forward scrolling after max itemOnScreen is reached
        if (maxChildViews == 0 || visibleItems < numberOfAnimatedItems ||
                // Reverse scrolling
                firstVisiblePosition in 2..maxChildViews ||
                // Reverse scrolling and click on FastScroller
                position > maxChildViews && firstVisiblePosition == -1 && mRecyclerView.childCount == 0) {

            // Base delay is step delay
            delay = mStepDelay
            if (visibleItems <= 1) {
                // When RecyclerView is initially loading no items are present
                // Use InitialDelay only for the first item
                delay += mInitialDelay
            } else {
                // Reset InitialDelay only when first item is already animated
                mInitialDelay = 0L
            }
            val numColumns = getFlexibleLayoutManager().getSpanCount()
            if (numColumns > 1) {
                delay = mInitialDelay + mStepDelay * (position % numColumns)
            }

        } else { // Forward scrolling before max itemOnScreen is reached
            delay = mInitialDelay + position * mStepDelay
        }
        return delay
    }

    /*---------------*/
    /* INNER CLASSES */
    /*---------------*/

    /**
     * Observer Class responsible to skip animation when items are notified to avoid
     * double animation with [RecyclerView.ItemAnimator].
     *
     * Also, some items at the edge, are rebound by Android and should not be animated.
     */
    inner class AnimatorAdapterDataObserver : RecyclerView.AdapterDataObserver() {
        var isPositionNotified: Boolean = false
            private set
        private val animatorHandler = Handler(Looper.getMainLooper()) {
            isPositionNotified = false
            true
        }

        fun clearNotified() {
            if (isPositionNotified) {
                animatorHandler.removeCallbacksAndMessages(null)
                animatorHandler.sendMessageDelayed(Message.obtain(animatorHandler), 200L)
            }
        }

        private fun markNotified() {
            isPositionNotified = true
        }

        override fun onChanged() {
            markNotified()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            markNotified()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            markNotified()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            markNotified()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            markNotified()
        }
    }

    /**
     * Helper Class to clear Animators List used to avoid multiple Item animation on same
     * position when fling.
     */
    inner class HelperAnimatorListener (private var hashCode: Int) : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
            sparseAnimators.remove(hashCode)
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}
    }
}