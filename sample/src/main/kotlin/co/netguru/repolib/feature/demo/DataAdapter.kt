package co.netguru.repolib.feature.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.netguru.repolib.R
import co.netguru.repolib.feature.demo.data.DemoDataEntity
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

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(
            holder: DataViewHolder,
            position: Int
    ) = holder.bind(items[position])

    fun remove(item: DemoDataEntity) {
        val index = this.items.indexOfFirst { it.id == item.id }
        if (index == -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun update(item: DemoDataEntity) {
        val newIndex = items.addOrUpdate(item)
        notifyItemChanged(newIndex)
    }

    fun add(item: DemoDataEntity) = items.addOrUpdate(item)

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
    val index = this.indexOfFirst { it.id == item.id }
    if (index == -1) {
        this.add(0, item)
    } else {
        set(index, item)
    }
    return index
}
