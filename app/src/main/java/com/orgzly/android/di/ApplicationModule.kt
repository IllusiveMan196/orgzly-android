package com.orgzly.android.di

import android.content.Context
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ApplicationModule {
    @Singleton
    @Provides
    fun providesResources(@ApplicationContext context: Context): Resources {
        return context.resources
    }
}