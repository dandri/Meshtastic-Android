/*
 * Copyright (c) 2025 Meshtastic LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.geeksville.mesh.repository.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.geeksville.mesh.AppOnlyProtos.ChannelSet
import com.geeksville.mesh.LocalOnlyProtos.LocalConfig
import com.geeksville.mesh.LocalOnlyProtos.LocalModuleConfig
import com.geeksville.mesh.repository.datastore.recentaddresses.RecentAddressesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private const val USER_PREFERENCES_NAME = "user_preferences"

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {
    @Singleton
    @Provides
    fun providePreferencesDataStore(
        @ApplicationContext appContext: Context
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler =
                ReplaceFileCorruptionHandler(produceNewData = { emptyPreferences() }),
            migrations = listOf(SharedPreferencesMigration(appContext, USER_PREFERENCES_NAME)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES_NAME) },
        )

    @Singleton
    @Provides
    fun provideRecentAddressesRepository(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>,
    ): RecentAddressesRepository = RecentAddressesRepository(context, dataStore)

    @Singleton
    @Provides
    fun provideLocalConfigDataStore(
        @ApplicationContext appContext: Context
    ): DataStore<LocalConfig> =
        DataStoreFactory.create(
            serializer = LocalConfigSerializer,
            produceFile = { appContext.dataStoreFile("local_config.pb") },
            corruptionHandler =
                ReplaceFileCorruptionHandler(produceNewData = { LocalConfig.getDefaultInstance() }),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )

    @Singleton
    @Provides
    fun provideModuleConfigDataStore(
        @ApplicationContext appContext: Context
    ): DataStore<LocalModuleConfig> =
        DataStoreFactory.create(
            serializer = ModuleConfigSerializer,
            produceFile = { appContext.dataStoreFile("module_config.pb") },
            corruptionHandler =
                ReplaceFileCorruptionHandler(
                    produceNewData = { LocalModuleConfig.getDefaultInstance() }
                ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )

    @Singleton
    @Provides
    fun provideChannelSetDataStore(@ApplicationContext appContext: Context): DataStore<ChannelSet> =
        DataStoreFactory.create(
            serializer = ChannelSetSerializer,
            produceFile = { appContext.dataStoreFile("channel_set.pb") },
            corruptionHandler =
                ReplaceFileCorruptionHandler(produceNewData = { ChannelSet.getDefaultInstance() }),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )
}
