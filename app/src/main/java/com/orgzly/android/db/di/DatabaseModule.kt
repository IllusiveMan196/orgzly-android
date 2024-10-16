package com.orgzly.android.db.di

import android.content.Context
import com.orgzly.android.db.OrgzlyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class DatabaseModule() {
    @Provides
    @Singleton
    internal fun provideDatabase(@ApplicationContext context: Context): OrgzlyDatabase {
        return OrgzlyDatabase.forFile(context, OrgzlyDatabase.NAME)
    }
}