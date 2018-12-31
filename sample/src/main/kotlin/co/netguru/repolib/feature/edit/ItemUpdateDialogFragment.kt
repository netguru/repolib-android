package co.netguru.repolib.feature.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import co.netguru.repolib.R
import co.netguru.repolib.feature.demo.data.DemoDataEntity
import co.netguru.repolib.feature.demo.di.UpdateViewModelFactory
import dagger.android.support.DaggerAppCompatDialogFragment
import kotlinx.android.synthetic.main.item_editor_layout.*
import javax.inject.Inject

class ItemUpdateDialogFragment : DaggerAppCompatDialogFragment() {

    companion object {
        const val ARG_ITEM = "arg:item"

        @JvmStatic
        fun newInstance(item: DemoDataEntity) = ItemUpdateDialogFragment()
                .apply { arguments = Bundle().apply { putParcelable(ARG_ITEM, item) } }
    }

    @Inject
    internal lateinit var factory: UpdateViewModelFactory
    private val updateItemViewModel: UpdateItemViewModel by lazy {
        ViewModelProviders.of(this, factory)[UpdateItemViewModel::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_editor_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveButton.setOnClickListener {
            updateItemViewModel.update(arguments?.get(ARG_ITEM) as DemoDataEntity) { dismiss() }
        }
    }
}