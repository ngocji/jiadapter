package com.jibase.iflexible.extensions

import android.content.Context
import androidx.annotation.DimenRes

fun Context.getDimensionPixelOffset(@DimenRes dimen: Int) = resources.getDimensionPixelOffset(dimen)
infix fun <T> List<T>.hasPosition(position: Int): Boolean = position in this.indices