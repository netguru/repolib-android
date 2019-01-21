package co.netguru.repolibrx.sample.application

import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.feature.demo.di.ConfigurationModule
import co.netguru.repolibrx.sample.feature.demo.di.DataLayerModule
import co.netguru.repolibrx.sample.feature.demo.di.MockingModule
import co.netguru.repolibrx.sample.feature.demo.di.datasources.RealmModule
import co.netguru.repolibrx.sample.feature.demo.di.datasources.RetrofitModule
import co.netguru.repolibrx.sample.feature.demo.di.datasources.RoomModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@AppScope
@Component(
        modules = [
            AndroidInjectionModule::class,
            AndroidSupportInjectionModule::class,
            ConfigurationModule::class,
            ActivityBindingModule::class,
            MockingModule::class,
            RoomModule::class,
            RealmModule::class,
            RetrofitModule::class,
            DataLayerModule::class
        ]
)
interface ApplicationComponent : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>() {
        abstract fun configurationModule(module: ConfigurationModule): Builder
    }
}
