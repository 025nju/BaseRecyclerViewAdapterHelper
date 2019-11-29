package com.chad.library.adapter.base.module

import androidx.annotation.IntRange
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.ExpandableEntity

/**
 * @author: limuyang
 * @date: 2019-11-29
 * @Description: 可以展开\折叠的模块
 */

/**
 * 需要【向上加载更多】功能的，[BaseQuickAdapter]继承此接口
 */
interface ExpandableModule

open class BaseExpandableModule<T>(private val baseQuickAdapter: BaseQuickAdapter<T, *>) {
    private fun recursiveExpand(position: Int, list: MutableList<T>): Int {
        var count = list.size
        var pos = position + list.size - 1
        var i = list.size - 1
        while (i >= 0) {
            val item = list[i]
            if (item is ExpandableEntity<*>) {
                if (item.isExpanded && hasSubItems(item)) {
                    val subList = item.subItems as MutableList<T>
                    baseQuickAdapter.data.addAll(pos + 1, subList)
                    val subItemCount = recursiveExpand(pos + 1, subList)
                    count += subItemCount
                }
            }
            i--
            pos--
        }
        return count
    }

    fun expand(@IntRange(from = 0) position: Int, animate: Boolean, shouldNotify: Boolean): Int {
        val adapterPos = position - baseQuickAdapter.getHeaderLayoutCount()

        val expandable = getExpandableItem(adapterPos) ?: return 0
        if (!hasSubItems(expandable)) {
            expandable.isExpanded = true
            baseQuickAdapter.notifyItemChanged(position)
            return 0
        }
        var subItemCount = 0
        if (!expandable.isExpanded) {
            val list = expandable.subItems as MutableList<T>
            baseQuickAdapter.data.addAll(adapterPos + 1, list)
            subItemCount += recursiveExpand(adapterPos + 1, list)

            expandable.isExpanded = true
            //            subItemCount += list.size();
        }
        if (shouldNotify) {
            if (animate) {
                baseQuickAdapter.notifyItemChanged(position)
                baseQuickAdapter.notifyItemRangeInserted(position + 1, subItemCount)
            } else {
                baseQuickAdapter.notifyDataSetChanged()
            }
        }
        return subItemCount
    }

    /**
     * Expand an expandable item
     *
     * @param position position of the item, which includes the header layout count.
     * @param animate  expand items with animation
     * @return the number of items that have been added.
     */
    fun expand(@IntRange(from = 0) position: Int, animate: Boolean): Int {
        return expand(position, animate, true)
    }

    /**
     * Expand an expandable item with animation.
     *
     * @param position position of the item, which includes the header layout count.
     * @return the number of items that have been added.
     */
    fun expand(@IntRange(from = 0) position: Int): Int {
        return expand(position, animate = true, shouldNotify = true)
    }

    fun expandAll(position: Int, animate: Boolean, notify: Boolean): Int {
        val adapterPos = position - baseQuickAdapter.getHeaderLayoutCount()

        val expandable = getExpandableItem(adapterPos) ?: return 0

        if (!hasSubItems(expandable)) {
            expandable.isExpanded = true
            baseQuickAdapter.notifyItemChanged(position)
            return 0
        }

        var endItem: T? = null
        if (adapterPos + 1 < baseQuickAdapter.data.size) {
            endItem = baseQuickAdapter.getItem(adapterPos + 1)
        }

        var count = expand(position, animate = false, shouldNotify = false)
        for (i in adapterPos + 1 until baseQuickAdapter.data.size) {
            val item = baseQuickAdapter.getItem(i)

            if (item != null && item == endItem) {
                break
            }
            if (isExpandable(item)) {
                count += expand(i + baseQuickAdapter.getHeaderLayoutCount(), animate = false, shouldNotify = false)
            }
        }

        if (notify) {
            if (animate) {
                baseQuickAdapter.notifyItemChanged(position)
                baseQuickAdapter.notifyItemRangeInserted(position + 1, count)
            } else {
                baseQuickAdapter.notifyDataSetChanged()
            }
        }
        return count
    }

    /**
     * expand the item and all its subItems
     *
     * @param position position of the item, which includes the header layout count.
     * @param init     whether you are initializing the recyclerView or not.
     * if **true**, it won't notify recyclerView to redraw UI.
     * @return the number of items that have been added to the adapter.
     */
    fun expandAll(position: Int, init: Boolean): Int {
        return expandAll(position, true, !init)
    }

    fun expandAll() {
        for (i in baseQuickAdapter.data.size - 1 + baseQuickAdapter.getHeaderLayoutCount() downTo baseQuickAdapter.getHeaderLayoutCount()) {
            expandAll(i, animate = false, notify = false)
        }
    }

    private fun recursiveCollapse(@IntRange(from = 0) position: Int): Int {
        val item = baseQuickAdapter.getItem(position)
        if (item == null || !isExpandable(item)) {
            return 0
        }
        val expandable = item as ExpandableEntity<*>
        if (!expandable.isExpanded) {
            return 0
        }
        val collapseList = ArrayList<T>()
        val itemLevel = expandable.level
        var itemTemp: T
        var i = position + 1
        val n = baseQuickAdapter.data.size
        while (i < n) {
            itemTemp = baseQuickAdapter.data[i]
            if (itemTemp is ExpandableEntity<*> && itemTemp.level <= itemLevel) {
                break
            }
            collapseList.add(itemTemp)
            i++
        }
        baseQuickAdapter.data.removeAll(collapseList)
        return collapseList.size
    }

    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @param animate  collapse with animation or not.
     * @param notify   notify the recyclerView refresh UI or not.
     * @return the number of subItems collapsed.
     */
    fun collapse(@IntRange(from = 0) position: Int, animate: Boolean, notify: Boolean): Int {
        val adapterPos = position - baseQuickAdapter.getHeaderLayoutCount()

        val expandable = getExpandableItem(adapterPos) ?: return 0
        val subItemCount = recursiveCollapse(adapterPos)
        expandable.isExpanded = false
        if (notify) {
            if (animate) {
                baseQuickAdapter.notifyItemChanged(position)
                baseQuickAdapter.notifyItemRangeRemoved(position + 1, subItemCount)
            } else {
                baseQuickAdapter.notifyDataSetChanged()
            }
        }
        return subItemCount
    }

    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @return the number of subItems collapsed.
     */
    fun collapse(@IntRange(from = 0) position: Int): Int {
        return collapse(position, animate = true, notify = true)
    }

    /**
     * Collapse an expandable item that has been expanded..
     *
     * @param position the position of the item, which includes the header layout count.
     * @return the number of subItems collapsed.
     */
    fun collapse(@IntRange(from = 0) position: Int, animate: Boolean): Int {
        return collapse(position, animate, true)
    }

    private fun getExpandableItem(position: Int): ExpandableEntity<*>? {
        val item = baseQuickAdapter.getItem(position)
        return if (isExpandable(item)) {
            item as ExpandableEntity<*>
        } else {
            null
        }
    }

    fun isExpandable(item: T?): Boolean {
        return item != null && item is ExpandableEntity<*>
    }

    fun hasSubItems(item: ExpandableEntity<*>?): Boolean {
        if (item == null) {
            return false
        }
        val list = item.subItems
        return list.isNotEmpty()
    }

    /**
     * Get the parent item position of the IExpandable item
     *
     * @return return the closest parent item position of the IExpandable.
     * if the IExpandable item's level is 0, return itself position.
     * if the item's level is negative which mean do not implement this, return a negative
     * if the item is not exist in the data list, return a negative.
     */
    fun getParentPosition(item: T): Int {
        val position = baseQuickAdapter.getItemPosition(item)
        if (position == -1) {
            return -1
        }
        // if the item is IExpandable, return a closest IExpandable item position whose level smaller than this.
        // if it is not, return the closest IExpandable item position whose level is not negative
        val level = if (item is ExpandableEntity<*>) {
            item.level
        } else {
            Integer.MAX_VALUE
        }
        if (level == 0) {
            return position
        } else if (level == -1) {
            return -1
        }

        for (i in position downTo 0) {
            val temp = baseQuickAdapter.data[i]
            if (temp is ExpandableEntity<*>) {
                if (temp.level in 0 until level) {
                    return i
                }
            }
        }
        return -1
    }
}