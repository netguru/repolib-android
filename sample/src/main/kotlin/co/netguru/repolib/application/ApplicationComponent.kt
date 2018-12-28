package co.netguru.repolib.application

import co.netguru.repolib.application.scope.AppScope
import co.netguru.repolib.feature.demo.di.DataLayerModule
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
            DataLayerModule::class
        ]
)
interface ApplicationComponent : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}
