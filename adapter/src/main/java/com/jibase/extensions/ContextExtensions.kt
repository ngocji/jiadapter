package com.jibase.extensions

import android.content.Context
import androidx.annotation.DimenRes

fun Context.getDimensionPixelOffset(@DimenRes dimen: Int) = resources.getDimensionPixelOffset(dimen)