package co.netguru.repolib.feature.demo

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.netguru.repolib.R
import co.netguru.repolib.feature.demo.di.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import org.jetbrains.anko.longToast
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var factory: ViewModelFactory

    private val demoViewModel: DemoViewModel by lazy {
        ViewModelProviders.of(this, factory)[DemoViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        setContentView(R.layout.main_activity)
        super.onCreate(savedInstanceState)

        val adapter = DataAdapter(demoViewModel.itemActionSubject())
        recyclerView.adapter = adapter
        swipeToRefresh.setOnRefreshListener { demoViewModel.refresh() }

        demoViewModel.data().observe(this, Observer { viewData ->
            swipeToRefresh.isRefreshing = false
            adapter.apply {
                this.items = viewData.items.reversed()
                viewData.error?.let { longToast(it) }
                notifyDataSetChanged()
            }
        })

        addButton.setOnClickListener {
            demoViewModel.addNew(addNewEditText.text.toString())
        }
    }
}

