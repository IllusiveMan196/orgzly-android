package com.orgzly.android.data.di

import com.orgzly.android.data.logs.AppLogsRepository
import com.orgzly.android.data.logs.DatabaseAppLogsRepository
import com.orgzly.android.db.OrgzlyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal open class DataModule {
    @Provides
    @Singleton
    internal fun providesLogsRepository(database: OrgzlyDatabase): AppLogsRepository {
        return DatabaseAppLogsRepository(database)
    }
}