package co.netguru.repolib.feature.demo

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.netguru.repolib.R
import co.netguru.repolib.feature.demo.data.UNDEFINED
import co.netguru.repolib.feature.demo.di.DemoViewModelFactory
import co.netguru.repolib.feature.edit.ItemUpdateDialogFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import org.jetbrains.anko.longToast
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var factoryDemo: DemoViewModelFactory
    private val demoViewModel: DemoViewModel by lazy {
        ViewModelProviders.of(this, factoryDemo)[DemoViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        setContentView(R.layout.main_activity)
        super.onCreate(savedInstanceState)

        val adapter = DataAdapter(demoViewModel.removeSubject(), demoViewModel.updateSubject())
        recyclerView.adapter = adapter
        swipeToRefresh.setOnRefreshListener { demoViewModel.refresh() }

        demoViewModel.data().observe(this, Observer { viewData ->
            swipeToRefresh.isRefreshing = false
            adapter.apply {
                this.items = viewData.items.reversed().toMutableList()
                viewData.error?.let { longToast(it) }
                if (viewData.indexToRemove != UNDEFINED.toInt()) adapter.remove(viewData.indexToRemove)
                notifyDataSetChanged()
            }
        })

        demoViewModel.dataToEdit().observe(this, Observer {
            ItemUpdateDialogFragment.newInstance(it).show(supportFragmentManager, null)
        })

        addButton.setOnClickListener {
            demoViewModel.addNew(addNewEditText.text.toString())
        }

        demoViewModel.refresh()
    }
}

