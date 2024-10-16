package com.orgzly.android.db.di

import android.content.Context
import com.orgzly.android.db.OrgzlyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
internal class TestDatabaseModule() {
    @Provides
    @Singleton
    internal fun provideDatabase(@ApplicationContext context: Context): OrgzlyDatabase {
        return OrgzlyDatabase.forFile(context, OrgzlyDatabase.NAME_FOR_TESTS)
    }
}