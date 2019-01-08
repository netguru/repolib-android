package co.netguru.repolibrx.sample.feature.demo

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.data.UNDEFINED
import io.reactivex.subjects.PublishSubject

class DataAdapter : RecyclerView.Adapter<DataViewHolder>() {

    private val items = mutableListOf<DemoDataEntity>()

    val removeSubject: PublishSubject<DemoDataEntity> = PublishSubject.create()
    val updateSubject: PublishSubject<DemoDataEntity> = PublishSubject.create()

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): DataViewHolder = DataViewHolder(parent, R.layout.item_layout)

    override fun onBindViewHolder(
            holder: DataViewHolder,
            position: Int
    ) {
        val item = items[position]
        holder.bind(item, { removeSubject.onNext(item) }, { updateSubject.onNext(item) })
    }

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
