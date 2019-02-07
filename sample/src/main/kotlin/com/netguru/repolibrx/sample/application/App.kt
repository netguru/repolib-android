package com.netguru.repolibrx.sample.application

import androidx.multidex.MultiDex
import com.netguru.repolibrx.sample.feature.demo.di.ConfigurationModule
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.realm.Realm
import timber.log.Timber

class App : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        Timber.plant(Timber.DebugTree())
        Realm.init(this)
    }

    override fun applicationInjector(): AndroidInjector<App> =
            DaggerApplicationComponent.builder()
                    .configurationModule(ConfigurationModule(this)).create(this)
}
