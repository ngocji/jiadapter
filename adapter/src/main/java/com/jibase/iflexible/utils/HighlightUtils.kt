package com.jibase.iflexible.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import com.jibase.R


object HighlightUtils {
    private const val SPLIT_EXPRESSION = "([, ]+)"
    private var colorAccent = -1

    /**
     * Make highlight of text in originalText
     * @param view the target of text view
     * @param constraint is text want highlight
     * @param hasBold make bold when highlight. default true
     * @param originalText
     * @param color custom color of highlight. default is [colorAccent]
     */
    @JvmStatic
    fun highlightText(view: TextView,
                      constraint: String,
                      hasBold: Boolean = true,
                      originalText: String = view.text.toString(),
                      color: Int = fetchAccentColor(view.context)
    ) {
        val constraintFix = constraint.lowercase()
        val start = originalText.lowercase().indexOf(constraintFix)
        if (start != -1) {
            val spanText = Spannable.Factory.getInstance().newSpannable(originalText)
            spanText(originalText, constraintFix, color, start, spanText, hasBold)
            view.setText(spanText, TextView.BufferType.SPANNABLE)
        } else {
            view.setText(originalText, TextView.BufferType.NORMAL)
        }
    }

    /**
     * Highlight of words in originalText
     * @param view the target of text view
     * @param constraint is text want highlight
     * @param hasBold make bold when highlight. default true
     * @param originalText
     * @param color custom color of highlight. default is [colorAccent]
     */
    @JvmStatic
    fun highlightWords(view: TextView,
                       constraint: String,
                       hasBold: Boolean = true,
                       originalText: String = view.text.toString(),
                       color: Int = fetchAccentColor(view.context)
    ) {
        val constraintsNormalizer = constraint.lowercase()
        val originalTextNormalizer = originalText.lowercase()

        var spanText: Spannable? = null

        for (word in constraintsNormalizer.split(SPLIT_EXPRESSION.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val start = originalTextNormalizer.indexOf(word)
            if (start != -1) {
                if (spanText == null) {
                    spanText = Spannable.Factory.getInstance().newSpannable(originalText)
                }
                spanText?.also {
                    spanText(originalText, word, color, start, it, hasBold)
                }
            }
        }

        if (spanText != null) {
            view.setText(spanText, TextView.BufferType.SPANNABLE)
        } else {
            view.setText(originalText, TextView.BufferType.NORMAL)
        }
    }


    private fun spanText(originalText: String,
                         constraint: String,
                         @ColorInt color: Int,
                         start: Int,
                         spanText: Spannable,
                         hasBold: Boolean) {
        val originalTextFix = originalText.lowercase()
        var newStart = start
        do {
            val end = newStart + constraint.length
            spanText.setSpan(ForegroundColorSpan(color), newStart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (hasBold) spanText.setSpan(StyleSpan(Typeface.BOLD), newStart, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            newStart = originalTextFix.indexOf(constraint, end + 1) // +1 skips the consecutive span
        } while (newStart != -1)
    }

    private fun fetchAccentColor(context: Context): Int {
        if (colorAccent == -1) {
            val typedValue = TypedValue()
            val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(androidx.appcompat.R.attr.colorAccent))
            colorAccent = a.getColor(0, 0)
            a.recycle()
        }
        return colorAccent
    }
}