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
package com.revenuecat.catpaywalls.feature.home

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.catpaywalls.core.data.ArticlesRepository
import com.revenuecat.catpaywalls.core.data.BookmarksRepository
import com.revenuecat.catpaywalls.core.data.PaywallsRepository
import com.revenuecat.catpaywalls.core.data.ReadingTrackerRepository
import com.revenuecat.catpaywalls.core.model.Article
import com.revenuecat.purchases.kmp.models.CustomerInfo
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class CatArticlesViewModel(
  articlesRepository: ArticlesRepository,
  paywallsRepository: PaywallsRepository,
  private val bookmarksRepository: BookmarksRepository,
  readingTrackerRepository: ReadingTrackerRepository,
) : ViewModel() {
  val uiState: StateFlow<HomeUiState> =
    articlesRepository
      .fetchArticles()
      .mapLatest { result ->
        result.fold(
          onSuccess = { HomeUiState.Success(it) },
          onFailure = { HomeUiState.Error(it.message) },
        )
      }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading,
      )

  val bookmarkedTitles: StateFlow<Set<String>> = bookmarksRepository.bookmarkedArticleTitles
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptySet(),
    )

  val customerInfo: StateFlow<CustomerInfo?> = paywallsRepository.fetchCustomerInfo()
    .map { it.getOrNull() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = null,
    )

  val todayReadCount: StateFlow<Int> = readingTrackerRepository.todayReadCount
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = 0,
    )

  fun toggleBookmark(articleTitle: String) {
    viewModelScope.launch { bookmarksRepository.toggleBookmark(articleTitle) }
  }
}

@Stable
sealed interface HomeUiState {
  data object Loading : HomeUiState

  data class Success(val articles: List<Article>) : HomeUiState

  data class Error(val message: String?) : HomeUiState
}
