package co.netguru.repolibrx.sample.feature.demo

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.netguru.repolibrx.sample.feature.demo.di.DemoViewModelFactory
import co.netguru.repolibrx.sample.feature.edit.ItemUpdateDialogFragment
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

        val adapter = DataAdapter()
        recyclerView.adapter = adapter
        swipeToRefresh.setOnRefreshListener {
            demoViewModel.refresh()
        }

        demoViewModel.setupUpdatingAction(adapter.updateSubject)
        demoViewModel.setupRemovingAction(adapter.removeSubject)

        demoViewModel.getData { viewData ->
            swipeToRefresh.isRefreshing = false
            adapter.add(viewData)
        }

        demoViewModel.updatedItemLiveData.observe(this, Observer {
            swipeToRefresh.isRefreshing = false
            adapter.update(it)
        })

        demoViewModel.editDataLiveData.observe(this, Observer {
            ItemUpdateDialogFragment.newInstance(it).show(supportFragmentManager, null)
        })

        demoViewModel.removedItemLiveData.observe(this, Observer {
            adapter.remove(it)
        })

        addButton.setOnClickListener {
            demoViewModel.addNew(addNewEditText.text.toString())
        }

        demoViewModel.stateLiveData.observe(this, Observer {
            swipeToRefresh.isRefreshing = false
            it.error?.let { message -> longToast(message) }
            adapter.notifyDataSetChanged()
        })

        demoViewModel.refresh()
    }
}

