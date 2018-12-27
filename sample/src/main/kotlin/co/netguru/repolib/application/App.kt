package co.netguru.repolib.application

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.realm.Realm
import javax.inject.Inject

class App : DaggerApplication() {

    @Inject
    lateinit var debugMetricsHelper: DebugMetricsHelper

    override fun onCreate() {
        super.onCreate()

        debugMetricsHelper.init(this)
        Realm.init(this)
    }

    override fun applicationInjector(): AndroidInjector<App> =
            DaggerApplicationComponent.builder().create(this)
}
