package co.netguru.repolibrx.sample.feature.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.di.UpdateViewModelFactory
import dagger.android.support.DaggerAppCompatDialogFragment
import kotlinx.android.synthetic.main.item_editor_layout.*
import org.jetbrains.anko.longToast
import javax.inject.Inject

class ItemUpdateDialogFragment : DaggerAppCompatDialogFragment() {
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
        val item = arguments?.get(ARG_ITEM) as DemoDataEntity
        itemEditTextInputLayout.setText(item.value)
        saveButton.setOnClickListener {
            updateItemViewModel.update(item.copy(value = itemEditTextInputLayout.text.toString()),
                    { dismiss() },
                    { th -> context?.longToast("error: ${th.message}") })
        }
    }

    companion object {
        const val ARG_ITEM = "arg:item"

        @JvmStatic
        fun newInstance(item: DemoDataEntity) = ItemUpdateDialogFragment()
                .apply { arguments = Bundle().apply { putParcelable(ARG_ITEM, item) } }
    }
}