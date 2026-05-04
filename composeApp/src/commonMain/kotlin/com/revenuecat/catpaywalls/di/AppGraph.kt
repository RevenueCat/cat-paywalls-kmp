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
package com.revenuecat.catpaywalls.di

import androidx.compose.runtime.Stable
import com.revenuecat.catpaywalls.core.data.ArticlesRepository
import com.revenuecat.catpaywalls.core.data.BookmarksRepository
import com.revenuecat.catpaywalls.core.data.PaywallsRepository
import com.revenuecat.catpaywalls.core.data.ReadingTrackerRepository
import com.revenuecat.catpaywalls.core.data.di.DataScope
import com.revenuecat.catpaywalls.core.data.di.createArticlesRepository
import com.revenuecat.catpaywalls.core.data.di.createBookmarksRepository
import com.revenuecat.catpaywalls.core.data.di.createPaywallsRepository
import com.revenuecat.catpaywalls.core.data.di.createReadingTrackerRepository
import com.revenuecat.catpaywalls.core.network.CatArticlesService
import com.revenuecat.catpaywalls.core.network.createHttpClient
import com.revenuecat.catpaywalls.core.network.di.NetworkScope
import com.revenuecat.catpaywalls.core.network.di.createCatArticlesService
import com.revenuecat.catpaywalls.feature.article.CatArticlesDetailViewModel
import com.revenuecat.catpaywalls.feature.bookmarks.BookmarksViewModel
import com.revenuecat.catpaywalls.feature.home.CatArticlesViewModel
import com.revenuecat.catpaywalls.feature.subscriptions.SubscriptionManagementViewModel
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Main application dependency graph.
 * This is the root graph that provides access to all app-level dependencies.
 * Network and Data implementations are internal to their modules and created via factory functions.
 */
@DependencyGraph(
  scope = AppScope::class,
  additionalScopes = [NetworkScope::class, DataScope::class],
)
@Stable
abstract class AppGraph {
  // ViewModels
  abstract val catArticlesViewModel: CatArticlesViewModel
  abstract val subscriptionManagementViewModel: SubscriptionManagementViewModel
  abstract val bookmarksViewModel: BookmarksViewModel

  // Factory for ViewModels that need runtime parameters
  abstract val articleDetailViewModelFactory: CatArticlesDetailViewModel.Factory

  // Network layer
  @Provides
  @SingleIn(NetworkScope::class)
  fun provideHttpClient(): HttpClient = createHttpClient()

  @Provides
  @SingleIn(NetworkScope::class)
  fun provideCatArticlesService(httpClient: HttpClient): CatArticlesService = createCatArticlesService(httpClient)

  // Repository layer
  @Provides
  @SingleIn(DataScope::class)
  fun provideArticlesRepository(service: CatArticlesService): ArticlesRepository = createArticlesRepository(service)

  @Provides
  @SingleIn(DataScope::class)
  fun providePaywallsRepository(): PaywallsRepository = createPaywallsRepository()

  @Provides
  @SingleIn(DataScope::class)
  fun provideBookmarksRepository(): BookmarksRepository = createBookmarksRepository()

  @Provides
  @SingleIn(DataScope::class)
  fun provideReadingTrackerRepository(): ReadingTrackerRepository = createReadingTrackerRepository()
}
