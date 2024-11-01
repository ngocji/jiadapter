package com.jibase.iflexible.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.SparseArray
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jibase.iflexible.extensions.getDimensionPixelOffset
import com.jibase.iflexible.adapter.FlexibleAdapter
import com.jibase.iflexible.utils.LayoutUtils
import kotlin.math.roundToInt

open class FlexibleItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {
    private var mDecorations: SparseArray<ItemDecoration> = SparseArray() // viewType -> itemDeco
    private var mViewTypes: List<Int>? = null
    private val mDefaultDecoration = ItemDecoration()
    private var mOffset = 0
    private var mSectionOffset = 0
    private var mDividerOnLastItem = 1
    private var mSectionGapOnLastItem = 1
    private var withLeftEdge = false
    private var withTopEdge = false
    private var withRightEdge = false
    private var withBottomEdge = false

    open var mDivider: Drawable? = null
    open val mBounds = Rect()
    open var mDrawOver: Boolean = false
    open val ATTRS = intArrayOf(android.R.attr.listDivider)

    /*==========*/
    /* DIVIDERS */
    /*==========*/

    /**
     * Default Android divider will be used.
     *
     * @param viewTypes the specific ViewTypes for which the divider will be drawn OR do not
     * specify any to draw divider for all ViewTypes (default behavior).
     * @return this FlexibleItemDecoration instance so the call can be chained
     */
    fun withDefaultDivider(vararg viewTypes: Int): FlexibleItemDecoration {
        val styledAttributes = context.obtainStyledAttributes(ATTRS)
        mDivider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()
        mViewTypes = viewTypes.toList()
        return this
    }

    /**
     * Custom divider.
     *
     * @param resId     drawable resourceId that should be used as a divider
     * @param viewTypes the specific ViewTypes for which the divider will be drawn OR do not
     * specify any to draw divider for all ViewTypes (default behavior).
     * @return this FlexibleItemDecoration instance so the call can be chained
     */
    fun withDivider(@DrawableRes resId: Int, vararg viewTypes: Int): FlexibleItemDecoration {
        mDivider = ContextCompat.getDrawable(context, resId)
        mViewTypes = viewTypes.toList()
        return this
    }

    /**
     * Allows to draw the Divider after the last item.
     *
     * Default is `false` (don't draw).
     *
     * @param lastItem `true` to draw, `false` to not draw
     * @return this FlexibleItemDecoration instance so the call can be chained
     */
    fun withDrawDividerOnLastItem(lastItem: Boolean): FlexibleItemDecoration {
        mDividerOnLastItem = if (lastItem) 0 else 1
        return this
    }

    /**
     * Removes any divider previously set.
     *
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @since 1.0.0-b1
     */
    fun removeDivider(): FlexibleItemDecoration {
        mDivider = null
        return this
    }

    /**
     * Changes the mode to draw the divider.
     *
     * - When `false`, any content will be drawn before the item views are drawn, and will
     * thus appear *underneath* the views.
     * <br></br>- When `true`, any content will be drawn after the item views are drawn, and will
     * thus appear *over* the views.
     * Default value is false (drawn underneath).
     *
     * @param drawOver true to draw after the item has been added, false to draw underneath the item
     * @return this Divider, so the call can be chained
     */
    fun withDrawOver(drawOver: Boolean): FlexibleItemDecoration {
        this.mDrawOver = drawOver
        return this
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mDivider != null && !mDrawOver) {
            draw(c, parent)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mDivider != null && mDrawOver) {
            draw(c, parent)
        }
    }

    open fun draw(c: Canvas, parent: RecyclerView) {
        if (parent.layoutManager == null) {
            return
        }
        if (LayoutUtils.getOrientation(parent) == RecyclerView.VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    open fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(
                    left, parent.paddingTop, right,
                    parent.height - parent.paddingBottom
            )
        } else {
            left = 0
            right = parent.width
        }

        val itemCount = parent.childCount
        for (i in 0 until (itemCount - mDividerOnLastItem)) {
            val child = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(child)
            if (shouldDrawDivider(viewHolder)) {
                parent.getDecoratedBoundsWithMargins(child, mBounds)
                val bottom = mBounds.bottom + Math.round(child.translationY)
                val top = bottom - (mDivider?.intrinsicHeight ?: 0)
                mDivider?.setBounds(left, top, right, bottom)
                mDivider?.draw(canvas)
            }
        }
        canvas.restore()
    }

    open fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(
                    parent.paddingLeft, top,
                    parent.width - parent.paddingRight, bottom
            )
        } else {
            top = 0
            bottom = parent.height
        }

        val itemCount = parent.childCount
        for (i in 0 until (itemCount - mDividerOnLastItem)) {
            val child = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(child)
            if (shouldDrawDivider(viewHolder)) {
                parent.layoutManager!!.getDecoratedBoundsWithMargins(child, mBounds)
                val right = mBounds.right + child.translationX.roundToInt()
                val left = right - (mDivider?.intrinsicWidth ?: 0)
                mDivider?.setBounds(left, top, right, bottom)
                mDivider?.draw(canvas)
            }
        }
        canvas.restore()
    }

    /**
     * Allows to define for which items the divider should be drawn.
     *
     * Override to define custom logic. By default all viewTypes will have the divider.
     *
     * @param viewHolder the ViewHolder under analysis
     * @return `true` to draw the divider, `false` to skip the drawing
     */
    open fun shouldDrawDivider(viewHolder: RecyclerView.ViewHolder): Boolean {
        return mViewTypes == null || true == mViewTypes?.isEmpty() || true == mViewTypes?.contains(
                viewHolder.itemViewType
        )
    }


    /*==============================*/
    /* OFFSET & EDGES CONFIGURATION */
    /*==============================*/

    /**
     * Adds an extra offset at the end of each section.
     *
     * Works only with [FlexibleAdapter].
     *
     * @param sectionOffset the extra offset at the end of each section
     * @return this FlexibleItemDecoration instance so the call can be chained
     */
    fun withSectionGapOffset(@DimenRes sectionOffset: Int): FlexibleItemDecoration {
        mSectionOffset = context.getDimensionPixelOffset(sectionOffset)
        return this
    }

    /**
     * Allows to disable the SectionGap after the last item.
     *
     * Default is `true` (draw).
     *
     * @param lastItem `true` to draw, `false` to not draw
     * @return this FlexibleItemDecoration instance so the call can be chained
     */
    fun withSectionGapOnLastItem(lastItem: Boolean): FlexibleItemDecoration {
        mSectionGapOnLastItem = if (lastItem) 1 else 0
        return this
    }

    /**
     * Adds an offset to the items at the 4 edges (left, top, right and bottom).
     *
     * **Note: **An offset OR an itemType with offsets must be added.
     * Default value is `false` (no edge).
     *
     * @param withEdge true to enableItem, false otherwise
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see .withOffset
     * @see .addItemViewType
     */
    fun withEdge(withEdge: Boolean): FlexibleItemDecoration {
        withLeftEdge = withEdge
        withTopEdge = withEdge
        withRightEdge = withEdge
        withBottomEdge = withEdge
        return this
    }

    /**
     * Adds an offset to the items at the Left edge.
     *
     * **Note: **An offset OR an itemType with offsets must be added.
     * Default value is `false` (no edge).
     *
     * @param withLeftEdge true to add offset to the items at the Left of the first column.
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see .withOffset
     * @see .addItemViewType
     */
    fun withLeftEdge(withLeftEdge: Boolean): FlexibleItemDecoration {
        this.withLeftEdge = withLeftEdge
        return this
    }

    /**
     * Adds an offset to the items at the Top edge.
     *
     * **Note: **An offset OR an itemType with offsets must be added.
     * Default value is `false` (no edge).
     *
     * @param withTopEdge true to add offset to the items at the Top of the first row.
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see .withOffset
     * @see .addItemViewType
     */
    fun withTopEdge(withTopEdge: Boolean): FlexibleItemDecoration {
        this.withTopEdge = withTopEdge
        return this
    }

    /**
     * Adds an offset to the items at the Bottom edge.
     *
     * **Note: **An offset OR an itemType with offsets must be added.
     * Default value is `false` (no edge).
     *
     * @param withBottomEdge true to add offset to the items at the Bottom of the last row.
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see .withOffset
     * @see .addItemViewType
     */
    fun withBottomEdge(withBottomEdge: Boolean): FlexibleItemDecoration {
        this.withBottomEdge = withBottomEdge
        return this
    }

    /**
     * Adds an offset to the items at the Right edge.
     *
     * **Note: **An offset OR an itemType with offsets must be added.
     * Default value is `false` (no edge).
     *
     * @param withRightEdge true to add offset to the items at the Right of the last column.
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see .withOffset
     * @see .addItemViewType
     */
    fun withRightEdge(withRightEdge: Boolean): FlexibleItemDecoration {
        this.withRightEdge = withRightEdge
        return this
    }

    /**
     * Returns the current general offset in dpi.
     *
     * @return the offset previously set in dpi
     */
    fun getOffset(): Int {
        return mOffset
    }

    /**
     * Applies the <u>same</u> physical offset to all sides of the item AND between items.
     *
     * @param offset the offset in dpi to apply
     * @return this FlexibleItemDecoration instance so the call can be chained
     */
    fun withOffset(@DimenRes offset: Int): FlexibleItemDecoration {
        mOffset = context.getDimensionPixelOffset(offset)
        return this
    }

    /**
     * Applies the general offset also to the specified viewType.
     *
     * Call [.withOffset] to set a general offset.
     *
     * @param viewType the viewType affected
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see .withOffset
     * @see .addItemViewType
     * @see .removeItemViewType
     */
    fun addItemViewType(@LayoutRes viewType: Int): FlexibleItemDecoration {
        return addItemViewType(viewType, -1)
    }

    /**
     * As [.addItemViewType] but with custom offset equals to all sides that will
     * affect only this viewType.
     *
     * @param viewType the viewType affected
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see .withOffset
     * @see .removeItemViewType
     */
    fun addItemViewType(@LayoutRes viewType: Int, @DimenRes offset: Int): FlexibleItemDecoration {
        return addItemViewType(viewType, offset, offset, offset, offset)
    }

    /**
     * As [.addItemViewType] but with custom offset that will affect only this viewType.
     *
     * @param viewType the viewType affected
     * @param left     the offset to the left of the item
     * @param top      the offset to the top of the item
     * @param right    the offset to the right of the item
     * @param bottom   the offset to the bottom of the item
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see .addItemViewType
     * @see .removeItemViewType
     */
    fun addItemViewType(
            @LayoutRes viewType: Int,
            @DimenRes left: Int,
            @DimenRes top: Int,
            @DimenRes right: Int,
            @DimenRes bottom: Int
    ): FlexibleItemDecoration {
        mDecorations.put(
                viewType, ItemDecoration(
                context.getDimensionPixelOffset(left),
                context.getDimensionPixelOffset(top),
                context.getDimensionPixelOffset(right),
                context.getDimensionPixelOffset(bottom)
        )
        )
        return this
    }

    /**
     * In case a viewType should not have anymore the applied offset.
     *
     * @param viewType the viewType to remove from the decoration management
     * @return this FlexibleItemDecoration instance so the call can be chained
     */
    fun removeItemViewType(@LayoutRes viewType: Int): FlexibleItemDecoration {
        mDecorations.remove(viewType)
        return this
    }

    /*====================*/
    /* OFFSET CALCULATION */
    /*====================*/

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            recyclerView: RecyclerView,
            state: RecyclerView.State
    ) {
        val position = recyclerView.getChildAdapterPosition(view)
        // Skip check so on item deleted, offset is kept (only if general offset was set!)
        // if (position == RecyclerView.NO_POSITION) return;

        // Get custom Item Decoration or default
        val adapter = recyclerView.adapter ?: return
        val itemType = if (position != RecyclerView.NO_POSITION) adapter.getItemViewType(position)
                ?: 0 else 0
        var deco = getItemDecoration(itemType)

        // No offset set, applies the general offset to this item decoration
        if (!deco.hasOffset()) {
            deco = ItemDecoration(mOffset)
        }

        // Default values (LinearLayout)
        var spanIndex = 0
        var spanSize = 1
        var spanCount = 1
        var orientation = RecyclerView.VERTICAL

        when (val layoutManager = recyclerView.layoutManager) {
            is GridLayoutManager -> {
                val lp = view.layoutParams as GridLayoutManager.LayoutParams
                spanIndex = lp.spanIndex
                spanSize = lp.spanSize
                spanCount = layoutManager.spanCount
                orientation = layoutManager.orientation
            }
            is StaggeredGridLayoutManager -> {
                val lp = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
                spanIndex = lp.spanIndex
                spanCount = layoutManager.spanCount
                spanSize = if (lp.isFullSpan) spanCount else 1
                orientation = layoutManager.orientation
            }
            is LinearLayoutManager -> {
                orientation = layoutManager.orientation
            }
        }

        val isFirstRowOrColumn = isFirstRowOrColumn(position, adapter, spanIndex, itemType)
        val isLastRowOrColumn = isLastRowOrColumn(position, adapter, spanIndex, spanCount, spanSize, itemType)

        // Reset offset values
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0

        if (orientation == GridLayoutManager.VERTICAL) {
            var index = spanIndex
            if (withLeftEdge) index = spanCount - spanIndex
            left = deco.left * index / spanCount

            index = spanCount - (spanIndex + spanSize - 1) - 1
            if (withRightEdge) index = spanIndex + spanSize
            right = deco.right * index / spanCount

            if (isFirstRowOrColumn && withTopEdge) {
                top = deco.top
            }
            if (isLastRowOrColumn) {
                if (withBottomEdge) {
                    bottom = deco.bottom
                }
            } else {
                bottom = deco.bottom
            }

        } else {
            var index = spanIndex
            if (withTopEdge) index = spanCount - spanIndex
            top = deco.top * index / spanCount

            index = spanCount - (spanIndex + spanSize - 1) - 1
            if (withBottomEdge) index = spanIndex + spanSize
            bottom = deco.bottom * index / spanCount

            if (isFirstRowOrColumn && withLeftEdge) {
                left = deco.left
            }
            if (isLastRowOrColumn) {
                if (withRightEdge) {
                    right = deco.right
                }
            } else {
                right = deco.right
            }
        }

        outRect.set(left, top, right, bottom)

        applySectionGap(outRect, adapter, position, orientation)
    }

    private fun getItemDecoration(itemType: Int): ItemDecoration {
        return mDecorations.get(itemType) ?: mDefaultDecoration
    }

    private fun isFirstRowOrColumn(
            position: Int,
            adapter: RecyclerView.Adapter<*>,
            spanIndex: Int,
            itemType: Int
    ): Boolean {
        val prePos = if (position > 0) position - 1 else -1
        // Last position on the last row
        val preRowPos = if (position > spanIndex) position - (1 + spanIndex) else -1
        // isFirstRowOrColumn if one of the following condition is true
        return position == 0 || prePos == -1 || itemType != adapter.getItemViewType(prePos) ||
                preRowPos == -1 || itemType != adapter.getItemViewType(preRowPos)
    }

    private fun isLastRowOrColumn(
            position: Int,
            adapter: RecyclerView.Adapter<*>,
            spanIndex: Int,
            spanCount: Int,
            spanSize: Int,
            itemType: Int
    ): Boolean {
        val itemCount = adapter.itemCount
        val nextPos = if (position < itemCount - 1) position + 1 else -1
        // First position on the next row
        val nextRowPos =
                if (position < itemCount - (spanCount / spanSize - spanIndex)) position + (spanCount / spanSize - spanIndex) else -1
        // isLastRowOrColumn if one of the following condition is true
        return position == itemCount - 1 || nextPos == -1 || itemType != adapter.getItemViewType(
                nextPos
        ) ||
                nextRowPos == -1 || itemType != adapter.getItemViewType(nextRowPos)
    }

    private fun applySectionGap(
            outRect: Rect,
            adapter: RecyclerView.Adapter<*>,
            position: Int,
            orientation: Int
    ) {
        // Section Gap Offset
        if (mSectionOffset > 0 && adapter is FlexibleAdapter<*>) {
            val nextItem = adapter.getItem(position + 1)

            // IMPORTANT: the check must be done on the BOTTOM of the section,
            // otherwise the sticky header will jump!
            if (adapter.isHeader(nextItem)) {
                //Log.v("applySectionGap position=%s", position);
                if (orientation == RecyclerView.VERTICAL) {
                    outRect.bottom += mSectionOffset
                } else {
                    outRect.right += mSectionOffset
                }
            }
            if (position >= adapter.itemCount - mSectionGapOnLastItem) {
                //Log.v("applySectionGapOnLastPosition position=%s", position);
                if (orientation == RecyclerView.VERTICAL) {
                    outRect.bottom += mSectionOffset
                } else {
                    outRect.right += mSectionOffset
                }
            }
        }
    }


    private class ItemDecoration(
            val left: Int = -1,
            val top: Int = -1,
            val right: Int = -1,
            val bottom: Int = -1
    ) {

        constructor(offset: Int) : this(offset, offset, offset, offset)

        fun hasOffset(): Boolean {
            return this.top >= 0 || this.left >= 0 || this.right >= 0 || this.bottom >= 0
        }
    }
}