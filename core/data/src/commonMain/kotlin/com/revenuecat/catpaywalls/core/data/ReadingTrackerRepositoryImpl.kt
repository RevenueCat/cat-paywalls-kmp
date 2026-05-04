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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ReadingTrackerRepositoryImpl(
  private val dataStore: DataStore<Preferences>,
  private val today: () -> String = {
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
  },
) : ReadingTrackerRepository {

  override val todayReadCount: Flow<Int> = dataStore.data.map { prefs ->
    val today = today()
    val lastReset = prefs[LAST_RESET_DATE_KEY]
    if (lastReset != today) 0 else prefs[READ_ARTICLES_KEY]?.size ?: 0
  }

  override suspend fun recordArticleRead(articleTitle: String) {
    dataStore.edit { prefs ->
      val today = today()
      val lastReset = prefs[LAST_RESET_DATE_KEY]
      if (lastReset != today) {
        prefs[LAST_RESET_DATE_KEY] = today
        prefs[READ_ARTICLES_KEY] = setOf(articleTitle)
      } else {
        val current = prefs[READ_ARTICLES_KEY] ?: emptySet()
        prefs[READ_ARTICLES_KEY] = current + articleTitle
      }
    }
  }

  private companion object {
    val LAST_RESET_DATE_KEY = stringPreferencesKey("last_reset_date")
    val READ_ARTICLES_KEY = stringSetPreferencesKey("read_articles_today")
  }
}
