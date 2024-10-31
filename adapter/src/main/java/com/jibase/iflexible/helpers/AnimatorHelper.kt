package com.jibase.iflexible.helpers

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView

object AnimatorHelper {
    /*-----------*/
    /* ANIMATORS */
    /*-----------*/

    /**
     * This is the default animator.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @param alphaFrom starting alpha value
     */
    @JvmStatic
    fun alphaAnimator(animators: MutableList<Animator>, view: View, @FloatRange(from = 0.0, to = 1.0) alphaFrom: Float) {
        view.alpha = 0f
        animators.add(ObjectAnimator.ofFloat(view, "alpha", alphaFrom, 1f))
    }

    /**
     * Item will slide from Left to Right.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @param percent   any % multiplier (between 0 and 1) of the LayoutManager Width
     */
    @JvmStatic
    fun slideInFromLeftAnimator(animators: MutableList<Animator>, view: View,
                                recyclerView: RecyclerView, @FloatRange(from = 0.0, to = 1.0) percent: Float) {
        alphaAnimator(animators, view, 0f)
        animators.add(ObjectAnimator.ofFloat(view, "translationX", -(recyclerView.layoutManager?.width
                ?: 0).toFloat() * percent, 0f))
        //Log.v("Added LEFT Animator");
    }

    /**
     * Item will slide from Right to Left.
     *
     * @param animators user defined list of animators
     * @param view      ItemView to animate
     * @param percent   Any % multiplier (between 0 and 1) of the LayoutManager Width
     */
    @JvmStatic
    fun slideInFromRightAnimator(
            animators: MutableList<Animator>, view: View,
            recyclerView: RecyclerView, @FloatRange(from = 0.0, to = 1.0) percent: Float) {
        alphaAnimator(animators, view, 0f)
        animators.add(ObjectAnimator.ofFloat(view, "translationX", (recyclerView.layoutManager?.width ?: 0).toFloat() * percent, 0f))
        //Log.v("Added RIGHT Animator");
    }

    /**
     * Item will slide from Top of the screen to its natural position.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     */
    @JvmStatic
    fun slideInFromTopAnimator(
            animators: MutableList<Animator>, view: View,
            recyclerView: RecyclerView) {
        alphaAnimator(animators, view, 0f)
        animators.add(ObjectAnimator.ofFloat(view, "translationY",( -recyclerView.measuredHeight shr 1).toFloat(), 0f))
        //Log.v("Added TOP Animator");
    }

    /**
     * Item will slide from Bottom of the screen to its natural position.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     */
    @JvmStatic
    fun slideInFromBottomAnimator(
            animators: MutableList<Animator>, view: View,
            recyclerView: RecyclerView) {
        alphaAnimator(animators, view, 0f)
        animators.add(ObjectAnimator.ofFloat(view, "translationY", (recyclerView.measuredHeight shr 1).toFloat(), 0f))
        //Log.v("Added BOTTOM Animator");
    }

    /**
     * Item will scale to `1.0f`.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     * @param scaleFrom initial scale value
     */
    @JvmStatic
    fun scaleAnimator(
            animators: MutableList<Animator>, view: View, @FloatRange(from = 0.0, to = 1.0) scaleFrom: Float) {
        alphaAnimator(animators, view, 0f)
        animators.add(ObjectAnimator.ofFloat(view, "scaleX", scaleFrom, 1f))
        animators.add(ObjectAnimator.ofFloat(view, "scaleY", scaleFrom, 1f))
        //Log.v("Added SCALE Animator");
    }

    /**
     * Item will flip from `0.0f` to `1.0f`.
     *
     * @param animators user defined list of animators
     * @param view      itemView to animate
     */
    @JvmStatic
    fun flipAnimator(animators: MutableList<Animator>, view: View) {
        alphaAnimator(animators, view, 0f)
        animators.add(ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f))
        //Log.v("Added FLIP Animator");
    }

    /**
     * Adds a custom duration to the current view.
     *
     * @param animators user defined list of animators
     * @param duration  duration in milliseconds
     */
    @JvmStatic
    fun setDuration(animators: List<Animator>, @IntRange(from = 0) duration: Long) {
        if (animators.isNotEmpty()) {
            val animator = animators[animators.size - 1]
            animator.duration = duration
        }
    }
}