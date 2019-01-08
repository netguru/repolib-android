package co.netguru.repolibrx.sample.application

import co.netguru.repolibrx.sample.application.scope.AppScope
import co.netguru.repolibrx.sample.feature.demo.di.DataLayerModule
import co.netguru.repolibrx.sample.feature.demo.di.MockingModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@AppScope
@Component(
        modules = [
            AndroidInjectionModule::class,
            AndroidSupportInjectionModule::class,
            ActivityBindingModule::class,
            MockingModule::class,
            DataLayerModule::class
        ]
)
interface ApplicationComponent : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>() {
        abstract fun mockingModule(module: MockingModule): Builder
    }
}
