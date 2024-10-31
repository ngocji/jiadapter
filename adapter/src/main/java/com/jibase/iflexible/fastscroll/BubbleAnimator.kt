package com.jibase.iflexible.fastscroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.View.VISIBLE
import com.jibase.extensions.isState
import com.jibase.extensions.invisible
import com.jibase.extensions.visible

class BubbleAnimator(var bubble: View, var durationInMillis: Long) {
    lateinit var animator: ObjectAnimator
    var isAnimating: Boolean = false

    fun showBubble() {
        if (isAnimating) {
            animator.cancel()
        }

        if (!bubble.isState(VISIBLE)) {
            bubble.visible()
            if (isAnimating) {
                animator.cancel()
            }
            animator = createShowAnimator(bubble)
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    onShowAnimationStop(bubble)
                    isAnimating = false
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    onShowAnimationStop(bubble)
                    isAnimating = false
                }
            })

            animator.start()
            isAnimating = true
        }
    }

    fun hideBubble() {
        if (isAnimating) {
            animator.cancel()
        }

        animator = createHideAnimator(bubble)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                onHideAnimationStop(bubble)
                isAnimating = false
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onHideAnimationStop(bubble)
                isAnimating = false
            }
        })

        animator.start()
        isAnimating = true
    }

     fun createShowAnimator(bubble: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(bubble, "alpha", 0f, 1f).setDuration(durationInMillis)
    }

     fun createHideAnimator(bubble: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(bubble, "alpha", 1f, 0f).setDuration(durationInMillis)
    }

     fun onShowAnimationStop(bubble: View) {
        // do nothing
    }

     fun onHideAnimationStop(bubble: View) {
        bubble.invisible()
    }

}