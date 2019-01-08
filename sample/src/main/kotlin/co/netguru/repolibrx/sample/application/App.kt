package co.netguru.repolibrx.sample.application

import co.netguru.repolibrx.sample.feature.demo.di.MockingModule
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.realm.Realm
import timber.log.Timber

class App : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Realm.init(this)
    }

    override fun applicationInjector(): AndroidInjector<App> =
            DaggerApplicationComponent.builder()
                    .mockingModule(MockingModule(this)).create(this)
}
