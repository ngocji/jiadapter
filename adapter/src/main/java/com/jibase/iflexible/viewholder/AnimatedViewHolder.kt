package com.jibase.iflexible.viewholder

import androidx.core.view.ViewPropertyAnimatorListener

interface AnimatedViewHolder{

    /**
     * Prepares the View for Add Animation. If this method is implemented and returns
     * `true`, then this method is performed against
     * `FlexibleItemAnimator#preAnimateAddImpl(RecyclerView.ViewHolder)` which will be ignored.
     *
     * Default value is `false`.
     *
     * @return `true` to confirm the execution of [.animateAddImpl],
     * of this class, `false` to use generic animation for all types of View.
     */
     fun preAnimateAddImpl(): Boolean

    /**
     * Prepares the View for Remove Animation. If this method is implemented and returns
     * `true`, then this method is performed against
     * `FlexibleItemAnimator#preAnimateRemoveImpl(RecyclerView.ViewHolder)` which will be ignored.
     *
     * Default value is `false`.
     *
     * @return `true` to confirm the execution of [.animateRemoveImpl],
     * of this class, `false` to use generic animation for all types of View.
     */
     fun preAnimateRemoveImpl(): Boolean

    /**
     * Animates this ViewHolder with this specific Add Animation.
     *
     * By returning `true` this ViewHolder will perform customized animation, while by
     * returning `false` generic animation is applied also for this ViewHolder.
     *
     * @param listener    should assign to `ViewCompat.animate().setListener(listener)`
     * @param addDuration duration of add animation
     * @param index       order of execution, starts with 0
     * @return `true` to animate with this implementation, `false` to use the generic
     * animation.
     */
     fun animateAddImpl(listener: ViewPropertyAnimatorListener, addDuration: Long, index: Int): Boolean

    /**
     * Animates this ViewHolder with this specific Remove Animation.
     *
     * By returning `true` this ViewHolder will perform customized animation, while by
     * returning `false` generic animation is applied also for this ViewHolder.
     *
     * @param listener       should assign to `ViewCompat.animate().setListener(listener)`
     * @param removeDuration duration of remove animation
     * @param index          order of execution, starts with 0  @return `true` to animate with this
     * implementation, `false` to use the generic animation.
     */
     fun animateRemoveImpl(listener: ViewPropertyAnimatorListener, removeDuration: Long, index: Int): Boolean
}