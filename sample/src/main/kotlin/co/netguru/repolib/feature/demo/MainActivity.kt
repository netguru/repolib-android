package co.netguru.repolib.feature.demo

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.netguru.repolib.R
import co.netguru.repolib.feature.demo.di.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var factory: ViewModelFactory
    val adapter = DataAdapter()

    private val demoViewModel: DemoViewModel by lazy {
        ViewModelProviders.of(this, factory)[DemoViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        setContentView(R.layout.main_activity)
        super.onCreate(savedInstanceState)

        swipeToRefresh.setOnRefreshListener { demoViewModel.refresh() }
        recyclerView.adapter = adapter

        demoViewModel.data().observe(this, Observer { items ->
            swipeToRefresh.isRefreshing = false
            adapter.apply {
                this.items = items
                notifyDataSetChanged()
            }
        })
    }
}
