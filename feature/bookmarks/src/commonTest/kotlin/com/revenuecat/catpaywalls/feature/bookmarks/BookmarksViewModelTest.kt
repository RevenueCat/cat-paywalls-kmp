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
package com.revenuecat.catpaywalls.feature.bookmarks

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarksViewModelTest {
  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeArticles: FakeArticlesRepository

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    fakeArticles = FakeArticlesRepository()
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun whenNoBookmarks_stateIsEmpty() = runTest {
    val articles = FakeArticlesRepository.createSampleArticles(count = 3)
    fakeArticles.setArticlesResult(Result.success(articles))
    val bookmarks = FakeBookmarksRepository()

    val viewModel = BookmarksViewModel(fakeArticles, bookmarks)

    viewModel.uiState.test {
      assertIs<BookmarksUiState.Loading>(awaitItem())
      testDispatcher.scheduler.advanceUntilIdle()
      assertIs<BookmarksUiState.Empty>(awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun whenBookmarksExist_stateIsSuccessWithFilteredArticles() = runTest {
    val articles = FakeArticlesRepository.createSampleArticles(count = 5)
    fakeArticles.setArticlesResult(Result.success(articles))
    val bookmarks = FakeBookmarksRepository(initial = setOf("Test Article 2", "Test Article 4"))

    val viewModel = BookmarksViewModel(fakeArticles, bookmarks)

    viewModel.uiState.test {
      assertIs<BookmarksUiState.Loading>(awaitItem())
      testDispatcher.scheduler.advanceUntilIdle()
      val success = awaitItem()
      assertIs<BookmarksUiState.Success>(success)
      assertEquals(2, success.articles.size)
      assertEquals(setOf("Test Article 2", "Test Article 4"), success.articles.map { it.title }.toSet())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun whenArticlesFetchFails_stateIsEmpty() = runTest {
    fakeArticles.setArticlesResult(Result.failure(RuntimeException("network down")))
    val bookmarks = FakeBookmarksRepository(initial = setOf("Test Article 1"))

    val viewModel = BookmarksViewModel(fakeArticles, bookmarks)

    viewModel.uiState.test {
      assertIs<BookmarksUiState.Loading>(awaitItem())
      testDispatcher.scheduler.advanceUntilIdle()
      assertIs<BookmarksUiState.Empty>(awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }
}
