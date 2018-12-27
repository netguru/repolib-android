package co.netguru.repolib.feature.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.netguru.repolib.R
import co.netguru.repolib.feature.demo.di.DataEntity
import kotlinx.android.synthetic.main.item_layout.view.*

class DataAdapter : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    var items = listOf<DataEntity>()

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): DataViewHolder = DataViewHolder(parent, R.layout.item_layout)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(
            holder: DataViewHolder,
            position: Int
    ) = holder.bind(items[position])

    class DataViewHolder(
            parent: ViewGroup,
            resId: Int
    ) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(resId, parent, false)
    ) {
        fun bind(dataViewHolder: DataEntity) {
            itemView.itemTitleTextView.text = dataViewHolder.value
        }
    }
}

