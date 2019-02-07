package com.netguru.repolibrx.sample.application

import com.netguru.repolibrx.sample.application.scope.ActivityScope
import com.netguru.repolibrx.sample.feature.demo.MainActivity
import com.netguru.repolibrx.sample.feature.edit.ItemUpdateDialogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivityBindingModule {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun mainActivityInjector(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun updateDialogFragment(): ItemUpdateDialogFragment
}