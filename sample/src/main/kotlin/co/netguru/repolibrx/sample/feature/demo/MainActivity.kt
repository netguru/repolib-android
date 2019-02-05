package co.netguru.repolibrx.sample.feature.demo

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.netguru.repolibrx.RepoLib
import co.netguru.repolibrx.sample.R
import co.netguru.repolibrx.sample.feature.demo.di.DemoViewModelFactory
import co.netguru.repolibrx.sample.feature.edit.ItemUpdateDialogFragment
import co.netguru.repolibrx.sample.feature.edit.UpdateItemViewModel
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import org.jetbrains.anko.longToast
import javax.inject.Inject

/**
 * This is [MainActivity] of the Sample app created to show example usage
 * of [RepoLib].
 * The sample App contains one Activity and one DialogFragment. The activity is responsible for
 * displaying and managing list of simple notes. This feature is written in MVVM pattern based on Android Architecture
 * Components ([DemoViewModel] and [LiveData]). The main business logic for this feature is placed
 * in [DemoViewModel]. So, to check Example usage of [RepoLib] check [DemoViewModel] class.
 *
 * Another feature of the app - updating selected Note is handled by the second of mentioned components
 * - [ItemUpdateDialogFragment]. It is responsible for updating item selected from the Notes list
 * presented in [MainActivity]. [ItemUpdateDialogFragment] it is also written using MVVM architecture
 * and DI pattern. The main business logic for this component is placed in [UpdateItemViewModel].
 */
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

