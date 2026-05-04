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

import com.revenuecat.catpaywalls.core.data.ArticlesRepository
import com.revenuecat.catpaywalls.core.data.ArticlesRepositoryImpl
import com.revenuecat.catpaywalls.core.data.BookmarksRepository
import com.revenuecat.catpaywalls.core.data.BookmarksRepositoryImpl
import com.revenuecat.catpaywalls.core.data.PaywallsRepository
import com.revenuecat.catpaywalls.core.data.PaywallsRepositoryImpl
import com.revenuecat.catpaywalls.core.data.ReadingTrackerRepository
import com.revenuecat.catpaywalls.core.data.ReadingTrackerRepositoryImpl
import com.revenuecat.catpaywalls.core.network.CatArticlesService

/**
 * Factory functions to create repositories.
 * This allows the implementations to remain internal while exposing public creation points.
 */
fun createArticlesRepository(service: CatArticlesService): ArticlesRepository = ArticlesRepositoryImpl(service)

fun createPaywallsRepository(): PaywallsRepository = PaywallsRepositoryImpl()

fun createBookmarksRepository(): BookmarksRepository = BookmarksRepositoryImpl(
  dataStore = createPreferencesDataStore(BOOKMARKS_DATA_STORE_NAME),
)

fun createReadingTrackerRepository(): ReadingTrackerRepository = ReadingTrackerRepositoryImpl(
  dataStore = createPreferencesDataStore(READING_TRACKER_DATA_STORE_NAME),
)
