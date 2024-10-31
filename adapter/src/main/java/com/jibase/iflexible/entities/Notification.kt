package com.jibase.iflexible.entities

data class Notification(val fromPosition: Int = -1, val toPosition: Int = -1, val operation: Int) {
    companion object {
        const val NONE = 0
        const val ADD = 1
        const val CHANGE = 2
        const val REMOVE = 3
        const val MOVE = 4
    }

    override fun toString(): String {
        return "Notification{" +
                "operation=" + operation +
                (if (operation == MOVE) ", fromPosition=$fromPosition" else "") +
                ", position=" + toPosition +
                '}'.toString()
    }
}