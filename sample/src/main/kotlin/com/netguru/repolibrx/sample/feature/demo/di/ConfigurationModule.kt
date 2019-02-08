package com.netguru.repolibrx.sample.feature.demo.di

import android.app.Application
import android.content.Context
import com.netguru.repolibrx.sample.application.scope.AppScope
import dagger.Module
import dagger.Provides

@Module
class ConfigurationModule(val application: Application) {

    @AppScope
    @Provides
    fun provideContext(): Context = application.applicationContext
}