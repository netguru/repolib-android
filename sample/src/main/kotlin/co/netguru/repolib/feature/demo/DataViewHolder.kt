package co.netguru.repolib.feature.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import kotlinx.android.synthetic.main.item_layout.view.*

class DataViewHolder(
        parent: ViewGroup,
        resId: Int
) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(resId, parent, false)
) {
    fun bind(
            demoDataViewHolder: DemoDataEntity,
            removeClickAction: () -> Unit,
            updateClick: () -> Unit
    ) = with(itemView) {
        itemTitleTextView.text = demoDataViewHolder.value
        sourceTextView.text = demoDataViewHolder.sourceType.name
        removeImageViewButton.setOnClickListener { removeClickAction() }
        itemContainer.setOnClickListener { updateClick() }
    }
}