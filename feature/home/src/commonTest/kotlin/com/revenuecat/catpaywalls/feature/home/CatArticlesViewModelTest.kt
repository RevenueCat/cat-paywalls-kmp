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
class CatArticlesViewModelTest {
  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepository: FakeArticlesRepository

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeArticlesRepository()
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialStateIsLoading() = runTest {
    // Given
    val articles = FakeArticlesRepository.createSampleArticles()
    fakeRepository.setArticlesResult(Result.success(articles))

    // When
    val viewModel = CatArticlesViewModel(fakeRepository)

    // Then
    viewModel.uiState.test {
      assertIs<HomeUiState.Loading>(awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun whenArticlesFetchSucceeds_stateIsSuccessWithArticles() = runTest {
    // Given
    val articles = FakeArticlesRepository.createSampleArticles(count = 5)
    fakeRepository.setArticlesResult(Result.success(articles))

    // When
    val viewModel = CatArticlesViewModel(fakeRepository)

    // Then
    viewModel.uiState.test {
      // Initial loading state
      assertIs<HomeUiState.Loading>(awaitItem())

      // Advance dispatcher to process the flow
      testDispatcher.scheduler.advanceUntilIdle()

      // Success state with articles
      val successState = awaitItem()
      assertIs<HomeUiState.Success>(successState)
      assertEquals(5, successState.articles.size)
      assertEquals("Test Article 1", successState.articles.first().title)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun whenArticlesFetchFails_stateIsErrorWithMessage() = runTest {
    // Given
    val errorMessage = "Network error"
    fakeRepository.setArticlesResult(Result.failure(Exception(errorMessage)))

    // When
    val viewModel = CatArticlesViewModel(fakeRepository)

    // Then
    viewModel.uiState.test {
      // Initial loading state
      assertIs<HomeUiState.Loading>(awaitItem())

      // Advance dispatcher to process the flow
      testDispatcher.scheduler.advanceUntilIdle()

      // Error state
      val errorState = awaitItem()
      assertIs<HomeUiState.Error>(errorState)
      assertEquals(errorMessage, errorState.message)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun whenArticlesListIsEmpty_stateIsSuccessWithEmptyList() = runTest {
    // Given
    fakeRepository.setArticlesResult(Result.success(emptyList()))

    // When
    val viewModel = CatArticlesViewModel(fakeRepository)

    // Then
    viewModel.uiState.test {
      // Initial loading state
      assertIs<HomeUiState.Loading>(awaitItem())

      // Advance dispatcher
      testDispatcher.scheduler.advanceUntilIdle()

      // Success state with empty list
      val successState = awaitItem()
      assertIs<HomeUiState.Success>(successState)
      assertEquals(0, successState.articles.size)

      cancelAndIgnoreRemainingEvents()
    }
  }
}
