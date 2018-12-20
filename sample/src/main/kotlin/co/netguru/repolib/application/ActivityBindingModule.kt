package co.netguru.repolib.application

import co.netguru.repolib.application.scope.ActivityScope
import co.netguru.repolib.feature.demo.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivityBindingModule {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun mainActivityInjector(): MainActivity
}