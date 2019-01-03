package co.netguru.repolib.feature.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.netguru.repolib.R
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.data.UNDEFINED
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_layout.view.*

class DataAdapter(
        private val removeSubject: PublishSubject<DemoDataEntity>,
        private val updateSubject: PublishSubject<DemoDataEntity>
) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    var items = mutableListOf<DemoDataEntity>()

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): DataViewHolder = DataViewHolder(parent, R.layout.item_layout, removeSubject, updateSubject)

    override fun onBindViewHolder(
            holder: DataViewHolder,
            position: Int
    ) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    fun add(item: DemoDataEntity) {
        items.addOrUpdate(item)
        notifyDataSetChanged()
    }

    fun remove(item: DemoDataEntity) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index != UNDEFINED.toInt()) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun update(item: DemoDataEntity) {
        val newIndex = items.addOrUpdate(item)
        notifyItemChanged(newIndex)
    }

    class DataViewHolder(
            parent: ViewGroup,
            resId: Int,
            private val publishSubject: PublishSubject<DemoDataEntity>,
            private val updateSubject: PublishSubject<DemoDataEntity>
    ) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(resId, parent, false)
    ) {
        fun bind(demoDataViewHolder: DemoDataEntity) = with(itemView) {
            itemTitleTextView.text = demoDataViewHolder.value
            sourceTextView.text = demoDataViewHolder.sourceType.name
            removeImageViewButton.setOnClickListener { publishSubject.onNext(demoDataViewHolder) }
            itemContainer.setOnClickListener { updateSubject.onNext(demoDataViewHolder) }
        }
    }
}

fun MutableList<DemoDataEntity>.addOrUpdate(item: DemoDataEntity): Int {
    val index = indexOfFirst { it.id == item.id }
    if (index == UNDEFINED.toInt()) {
        add(0, item)
    } else {
        set(index, item)
    }
    return index
}
