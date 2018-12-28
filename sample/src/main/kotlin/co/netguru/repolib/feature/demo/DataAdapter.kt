package co.netguru.repolib.feature.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.netguru.repolib.R
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_layout.view.*

class DataAdapter(private val publishSubject: PublishSubject<DemoDataEntity>) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    var items = listOf<DemoDataEntity>()

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): DataViewHolder = DataViewHolder(parent, R.layout.item_layout, publishSubject)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(
            holder: DataViewHolder,
            position: Int
    ) = holder.bind(items[position])

    class DataViewHolder(
            parent: ViewGroup,
            resId: Int,
            private val publishSubject: PublishSubject<DemoDataEntity>
    ) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(resId, parent, false)
    ) {
        fun bind(demoDataViewHolder: DemoDataEntity) = with(itemView) {
            itemTitleTextView.text = demoDataViewHolder.value
            sourceTextView.text = demoDataViewHolder.sourceType.name
            removeImageViewButton.setOnClickListener { publishSubject.onNext(demoDataViewHolder) }
        }
    }
}

