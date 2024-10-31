package com.jibase.iflexible.fastscroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.TRANSLATION_X
import com.jibase.extensions.isState
import com.jibase.extensions.invisible
import com.jibase.extensions.visible

class ScrollbarAnimator(var bar: View,
                        var handle: View,
                        var handleAlwaysVisible: Boolean,
                        var delayInMillis: Long,
                        var durationInMillis: Long) {
    private lateinit var scrollbarAnimatorSet: AnimatorSet
    var isAnimating: Boolean = false


    fun showScrollbar() {
        if (isAnimating) {
            scrollbarAnimatorSet.cancel()
        }

        if (bar.isState(INVISIBLE) || handle.isState(INVISIBLE)) {
            bar.visible()
            handle.visible()

            scrollbarAnimatorSet = createAnimator(bar, handle, true)
            scrollbarAnimatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    onShowAnimationStop(bar, handle)
                    isAnimating = false
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    onShowAnimationStop(bar, handle)
                    isAnimating = false
                }
            })
            scrollbarAnimatorSet.start()
            isAnimating = true
        }
    }

    fun hideScrollbar() {
        if (isAnimating) {
            scrollbarAnimatorSet.cancel()
        }

        scrollbarAnimatorSet = createAnimator(bar, handle, false)
        scrollbarAnimatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onHideAnimationStop(bar, handle)
                isAnimating = false
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                onHideAnimationStop(bar, handle)
                isAnimating = false
            }
        })
        scrollbarAnimatorSet.start()
        isAnimating = true
    }

    fun createAnimator(bar: View, handle: View, showFlag: Boolean): AnimatorSet {
        val barAnimator = ObjectAnimator.ofFloat(bar, TRANSLATION_X, if (showFlag) 0f else bar.width.toFloat())
        val animator = AnimatorSet()
        if (handleAlwaysVisible) {
            animator.play(barAnimator)
        } else {
            val handleAnimator = ObjectAnimator.ofFloat(handle, TRANSLATION_X, if (showFlag) 0f else handle.width.toFloat())
            animator.playTogether(barAnimator, handleAnimator)
        }
        animator.duration = durationInMillis
        if (!showFlag) {
            animator.startDelay = delayInMillis
        }
        return animator
    }

    fun onShowAnimationStop(bar: View, handle: View) {
        // do something
    }

    fun onHideAnimationStop(bar: View, handle: View) {
        bar.invisible()
        if (!handleAlwaysVisible) {
            handle.invisible()
        }
        bar.translationX = 0f
        handle.translationX = 0f
    }

}