/*
 * Copyright (c) 2025 RevenueCat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revenuecat.catpaywalls.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import okio.FileSystem
import okio.Path

/** Returns the platform-specific directory in which DataStore files should live. */
internal expect fun dataStoreDirectory(): Path

const val BOOKMARKS_DATA_STORE_NAME: String = "bookmarks.preferences_pb"
const val READING_TRACKER_DATA_STORE_NAME: String = "reading_tracker.preferences_pb"

fun createPreferencesDataStore(fileName: String): DataStore<Preferences> = PreferenceDataStoreFactory.create(
  storage = OkioStorage(
    fileSystem = FileSystem.SYSTEM,
    serializer = PreferencesSerializer,
    producePath = { dataStoreDirectory().resolve(fileName) },
  ),
)
