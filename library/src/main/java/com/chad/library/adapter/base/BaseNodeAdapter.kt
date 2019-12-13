package com.chad.library.adapter.base

import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.entity.node.NodeFooterImp
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder

abstract class BaseNodeAdapter<VH : BaseViewHolder>(data: MutableList<BaseNode>? = null)
    : BaseProviderMultiAdapter<BaseNode, VH>(data) {

    private val fullSpanNodeTypeSet = HashSet<Int>()

    init {
        if (!data.isNullOrEmpty()) {
            val flatData = flatData(data)
            data.clear()
            data.addAll(flatData)
        }
    }


    /**
     * 添加 node provider
     * @param provider BaseItemProvider
     */
    fun addNodeProvider(provider: BaseNodeProvider<VH>) {
        addItemProvider(provider)
    }

    /**
     * 添加需要铺满的 node provider
     * @param provider BaseItemProvider
     */
    fun addFullSpanNodeProvider(provider: BaseNodeProvider<VH>) {
        fullSpanNodeTypeSet.add(provider.itemViewType)
        addItemProvider(provider)
    }

    /**
     * 添加脚部 node provider
     * 铺满一行或者一列
     * @param provider BaseItemProvider
     */
    fun addFooterNodeProvider(provider: BaseNodeProvider<VH>) {
        addFullSpanNodeProvider(provider)
    }

    /**
     * 请勿直接通过此方法添加 node provider！
     * @param provider BaseItemProvider<BaseNode, VH>
     */
    override fun addItemProvider(provider: BaseItemProvider<BaseNode, VH>) {
        if (provider is BaseNodeProvider) {
            super.addItemProvider(provider)
        } else {
            throw IllegalStateException("please add BaseNodeProvider, no BaseItemProvider!")
        }
    }

    override fun isFixedViewType(type: Int): Boolean {
        return super.isFixedViewType(type) || fullSpanNodeTypeSet.contains(type)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        if (fullSpanNodeTypeSet.contains(viewType)) {
            setFullSpan(holder)
        }
        return holder
    }

    /*************************** 重写数据设置方法 ***************************/

    override fun setNewData(data: MutableList<BaseNode>?) {
        if (data == this.data) {
            return
        }
        super.setNewData(flatData(data ?: arrayListOf()))
    }

    override fun addData(position: Int, data: BaseNode) {
        addData(position, arrayListOf(data))
    }

    override fun addData(data: BaseNode) {
        addData(arrayListOf(data))
    }

    override fun addData(position: Int, newData: Collection<BaseNode>) {
        val nodes = flatData(newData)
        super.addData(position, nodes)
    }

    override fun addData(newData: Collection<BaseNode>) {
        val nodes = flatData(newData)
        super.addData(nodes)
    }

    override fun remove(position: Int) {
        if (position >= data.size) {
            return
        }

        //被移除的item数量
        var removeCount = 0

        val node = this.data[position]
        //移除子项
        if (!node.childNode.isNullOrEmpty()) {
            val items = flatData(node.childNode!!)
            this.data.removeAll(items)
            removeCount = items.size
        }
        //移除node自己
        this.data.removeAt(position)
        removeCount += 1

        // 移除脚部
        if (node is NodeFooterImp && node.footerNode != null) {
            this.data.removeAt(position)
            removeCount += 1
        }

        notifyItemRangeRemoved(position + getHeaderLayoutCount(), removeCount)
        compatibilityDataSizeChanged(0)
    }

    override fun setData(index: Int, data: BaseNode) {
        val flatData = flatData(arrayListOf(data))
        flatData.forEachIndexed { i, baseNode ->
            this.data[index + i] = baseNode
        }
        notifyItemRangeChanged(index + getHeaderLayoutCount(), flatData.size)
    }

    override fun replaceData(newData: Collection<BaseNode>) {
        // 不是同一个引用才清空列表
        if (newData != this.data) {
            super.replaceData(flatData(newData))
        }
    }

    override fun setDiffNewData(newData: MutableList<BaseNode>?) {
        if (hasEmptyView()) {
            setNewData(newData)
            return
        }
        super.setDiffNewData(flatData(newData ?: arrayListOf()))
    }

    override fun setDiffNewData(diffResult: DiffUtil.DiffResult, newData: MutableList<BaseNode>) {
        if (hasEmptyView()) {
            setNewData(newData)
            return
        }
        super.setDiffNewData(diffResult, flatData(newData))
    }

    /*************************** 重写数据设置方法 END ***************************/

    /**
     * 将输入的嵌套类型数组循环递归，数据扁平化
     * @param list List<BaseNode>
     * @param isSetExpanded 是否设置为展开状态，不变化设置为null
     * @return MutableList<BaseNode>
     */
    private fun flatData(list: Collection<BaseNode>): MutableList<BaseNode> {
        val newList = ArrayList<BaseNode>()

        for (element in list) {
            newList.add(element)

//            if (element is BaseExpandNode && !element.isExpanded) {
//                //什么都不做
//            } else {
//                val childNode = element.childNode
//                if (!childNode.isNullOrEmpty()) {
//                    val items = flatData(childNode, isExpanded)
//                    newList.addAll(items)
//                }
//            }

            if (element is BaseExpandNode) {
                if (element.isExpanded) {
                    val childNode = element.childNode
                    if (!childNode.isNullOrEmpty()) {
                        val items = flatData(childNode)
                        newList.addAll(items)
                    }
                }
            } else {
                val childNode = element.childNode
                if (!childNode.isNullOrEmpty()) {
                    val items = flatData(childNode)
                    newList.addAll(items)
                }
            }

            if (element is NodeFooterImp) {
                element.footerNode?.let {
                    newList.add(it)
                }
            }
        }

        return newList
    }

    /**
     * 在扁平化数据的同时，设置展开状态
     * @param list Collection<BaseNode>
     * @return MutableList<BaseNode>
     */
    private fun flatDataWhitSetExpanded(list: Collection<BaseNode>, isSetExpanded: Boolean? = null): MutableList<BaseNode> {
        val newList = ArrayList<BaseNode>()

        for (element in list) {
            newList.add(element)

            if (element is BaseExpandNode) {
                // TODO 判断有问题
                if (element.isExpanded) {
                    val childNode = element.childNode
                    if (!childNode.isNullOrEmpty()) {
                        val items = flatDataWhitSetExpanded(childNode, isSetExpanded)
                        newList.addAll(items)
                    }
                }
                if (isSetExpanded != null) {
                    val childNode = element.childNode
                    if (!childNode.isNullOrEmpty()) {
                        flatDataWhitSetExpanded(childNode, isSetExpanded)
                    }

                    element.isExpanded = isSetExpanded
                }

            } else {
                val childNode = element.childNode
                if (!childNode.isNullOrEmpty()) {
                    val items = flatDataWhitSetExpanded(childNode, isSetExpanded)
                    newList.addAll(items)
                }
            }

            if (element is NodeFooterImp) {
                element.footerNode?.let {
                    newList.add(it)
                }
            }
        }
        return newList
    }


    /**
     * 收起Node
     * @param position Int
     * @param animate Boolean
     * @param notify Boolean
     */
    @JvmOverloads
    fun collapse(@IntRange(from = 0) position: Int,
                 isChildExpand: Boolean? = null,
                 animate: Boolean = true,
                 notify: Boolean = true) {
        val node = this.data[position]

        if (node is BaseExpandNode && node.isExpanded) {
            val adapterPosition = position + getHeaderLayoutCount()

            node.isExpanded = false
            if (node.childNode.isNullOrEmpty()) {
                notifyItemChanged(adapterPosition)
                return
            }
            val items = flatDataWhitSetExpanded(node.childNode!!, isChildExpand)
            this.data.removeAll(items)
            if (notify) {
                if (animate) {
                    notifyItemChanged(adapterPosition)
                    notifyItemRangeRemoved(adapterPosition + 1, items.size)
                } else {
                    notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * 展开Node
     * @param position Int
     * @param animate Boolean
     * @param notify Boolean
     */
    @JvmOverloads
    fun expand(@IntRange(from = 0) position: Int,
               isChildExpand: Boolean? = null,
               animate: Boolean = true,
               notify: Boolean = true) {
        val node = this.data[position]

        if (node is BaseExpandNode && !node.isExpanded) {
            val adapterPosition = position + getHeaderLayoutCount()

            node.isExpanded = true
            if (node.childNode.isNullOrEmpty()) {
                notifyItemChanged(adapterPosition)
                return
            }
            val items = flatDataWhitSetExpanded(node.childNode!!, isChildExpand)
            this.data.addAll(position + 1, items)
            if (notify) {
                if (animate) {
                    notifyItemChanged(adapterPosition)
                    notifyItemRangeInserted(adapterPosition + 1, items.size)
                } else {
                    notifyDataSetChanged()
                }
            }

        }
    }

    /**
     * 收起或展开Node
     * @param position Int
     * @param animate Boolean
     * @param notify Boolean
     */
    @JvmOverloads
    fun expandOrCollapse(@IntRange(from = 0) position: Int, animate: Boolean = true, notify: Boolean = true) {
        val node = this.data[position]
        if (node is BaseExpandNode) {
            if (node.isExpanded) {
                collapse(position, null, animate, notify)
            } else {
                expand(position, null, animate, notify)
            }
        }
    }

    @JvmOverloads
    fun expandAndChild(@IntRange(from = 0) position: Int, animate: Boolean = true, notify: Boolean = true) {
        expand(position, true, animate, notify)
    }

    @JvmOverloads
    fun collapseAndChild(@IntRange(from = 0) position: Int, animate: Boolean = true, notify: Boolean = true) {
        collapse(position, false, animate, notify)
    }

    /**
     * 查找父节点。如果不存在，则返回-1
     * @param node BaseNode
     * @return Int 父节点的position
     */
    fun findParentNode(node: BaseNode): Int {
        val pos = this.data.indexOf(node)
        if (pos == -1 || pos == 0) {
            return -1
        }

        for (i in pos - 1 downTo 0) {
            val tempNode = this.data[i]
            if (tempNode.childNode?.contains(node) == true) {
                return i
            }
        }
        return -1
    }

    fun findParentNode(@IntRange(from = 0) position: Int): Int {
        if (position == 0) {
            return -1
        }
        val node = this.data[position]
        for (i in position - 1 downTo 0) {
            val tempNode = this.data[i]
            if (tempNode.childNode?.contains(node) == true) {
                return i
            }
        }
        return -1
    }
}