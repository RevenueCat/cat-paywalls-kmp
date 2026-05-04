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
package com.revenuecat.catpaywalls.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class BookmarksRepositoryImpl(private val dataStore: DataStore<Preferences>) : BookmarksRepository {

  override val bookmarkedArticleTitles: Flow<Set<String>> = dataStore.data.map { prefs ->
    prefs[BOOKMARKS_KEY] ?: emptySet()
  }

  override suspend fun toggleBookmark(articleTitle: String) {
    dataStore.edit { prefs ->
      val current = prefs[BOOKMARKS_KEY] ?: emptySet()
      prefs[BOOKMARKS_KEY] =
        if (articleTitle in current) current - articleTitle else current + articleTitle
    }
  }

  private companion object {
    val BOOKMARKS_KEY = stringSetPreferencesKey("bookmarked_articles")
  }
}
