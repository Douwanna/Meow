package top.rechinx.meow.ui.filter.items

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.items.ISectionable
import eu.davidea.viewholders.ExpandableViewHolder
import top.rechinx.meow.R
import top.rechinx.meow.core.source.model.Filter

class GroupItem(val filter: Filter.Group<*>) : AbstractExpandableHeaderItem<GroupItem.Holder, ISectionable<*, *>>() {

    init {
        isExpanded = false
    }

    override fun getLayoutRes(): Int {
        return R.layout.item_navigation_group
    }

    override fun getItemViewType(): Int {
        return 101
    }

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): Holder? {
        return Holder(view, adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?, holder: Holder, position: Int, payloads: MutableList<Any>?) {
        holder.title.text = filter.name

        holder.icon.setImageResource(if (isExpanded)
            R.drawable.ic_expand_more_black_24dp
        else
            R.drawable.ic_chevron_right_black_24dp)

        holder.itemView.setOnClickListener(holder)

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return filter == (other as GroupItem).filter
    }

    override fun hashCode(): Int {
        return filter.hashCode()
    }


    open class Holder(view: View, adapter: FlexibleAdapter<*>) : ExpandableViewHolder(view, adapter, true) {

        val title: TextView = itemView.findViewById(R.id.title)
        val icon: ImageView = itemView.findViewById(R.id.expandIcon)

        override fun shouldNotifyParentOnClick(): Boolean {
            return true
        }

    }
}