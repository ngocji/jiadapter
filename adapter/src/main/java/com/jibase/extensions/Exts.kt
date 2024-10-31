package com.jibase.extensions

infix fun <T> List<T>.hasPosition(position: Int): Boolean = position in this.indices