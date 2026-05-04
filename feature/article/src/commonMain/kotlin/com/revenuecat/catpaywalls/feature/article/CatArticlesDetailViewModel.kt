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
package com.revenuecat.catpaywalls.feature.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.catpaywalls.core.data.ArticlesRepository
import com.revenuecat.catpaywalls.core.data.BookmarksRepository
import com.revenuecat.catpaywalls.core.data.PaywallsRepository
import com.revenuecat.catpaywalls.core.data.ReadingTrackerRepository
import com.revenuecat.catpaywalls.core.model.Article
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatArticlesDetailViewModel(
  articleId: Long,
  articlesRepository: ArticlesRepository,
  private val paywallsRepository: PaywallsRepository,
  private val bookmarksRepository: BookmarksRepository,
  private val readingTrackerRepository: ReadingTrackerRepository,
) : ViewModel() {
  val article: StateFlow<Article?> =
    articlesRepository
      .getArticleById(articleId)
      .map { it.getOrNull() }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
      )

  val customerInfo: StateFlow<CustomerInfo?> =
    paywallsRepository
      .fetchCustomerInfo()
      .map { it.getOrNull() }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
      )

  val bookmarkedTitles: StateFlow<Set<String>> = bookmarksRepository.bookmarkedArticleTitles
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptySet(),
    )

  val todayReadCount: StateFlow<Int> = readingTrackerRepository.todayReadCount
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = 0,
    )

  val offering: StateFlow<Offering?> = paywallsRepository.fetchOffering()
    .map { it.getOrNull() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = null,
    )

  private val promoShownThisSession = MutableStateFlow(false)

  val shouldShowPromo: StateFlow<Boolean> = combine(
    todayReadCount,
    customerInfo,
    promoShownThisSession,
  ) { count, info, shown ->
    val isEntitled = info?.entitlements?.active?.isNotEmpty() == true
    count == FREE_DAILY_QUOTA && !isEntitled && !shown
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = false,
  )

  fun dismissPromo() {
    promoShownThisSession.value = true
  }

  fun purchasePackage(packageId: String) {
    paywallsRepository.awaitPurchase(packageId)
      .onEach { result ->
        result.fold(
          onSuccess = { dismissPromo() },
          onFailure = { },
        )
      }
      .launchIn(viewModelScope)
  }

  fun recordRead(articleTitle: String) {
    viewModelScope.launch { readingTrackerRepository.recordArticleRead(articleTitle) }
  }

  fun toggleBookmark(articleTitle: String, isPremium: Boolean, onNotPremium: () -> Unit) {
    if (!isPremium) {
      onNotPremium()
      return
    }
    viewModelScope.launch { bookmarksRepository.toggleBookmark(articleTitle) }
  }

  @Inject
  class Factory(
    private val articlesRepository: ArticlesRepository,
    private val paywallsRepository: PaywallsRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val readingTrackerRepository: ReadingTrackerRepository,
  ) {
    fun create(articleId: Long): CatArticlesDetailViewModel = CatArticlesDetailViewModel(
      articleId = articleId,
      articlesRepository = articlesRepository,
      paywallsRepository = paywallsRepository,
      bookmarksRepository = bookmarksRepository,
      readingTrackerRepository = readingTrackerRepository,
    )
  }

  private companion object {
    const val FREE_DAILY_QUOTA = 3
  }
}
