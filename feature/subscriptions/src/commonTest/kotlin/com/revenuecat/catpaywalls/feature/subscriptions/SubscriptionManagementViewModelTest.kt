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
package com.revenuecat.catpaywalls.feature.subscriptions

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

/**
 * Unit tests for [SubscriptionManagementViewModel].
 *
 * These tests use a [FakePaywallsRepository] to simulate various scenarios
 * without needing the actual RevenueCat SDK or Test Store.
 *
 * For integration tests with RevenueCat's Test Store, use instrumented tests
 * that can access the Android framework and initialize the RevenueCat SDK.
 *
 * @see <a href="https://www.revenuecat.com/blog/engineering/testing-test-store/">
 *   Simplify in-app purchase unit testing with RevenueCat's Test Store</a>
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionManagementViewModelTest {
  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepository: FakePaywallsRepository

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakePaywallsRepository()
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialStateIsLoading() = runTest {
    // When
    val viewModel = SubscriptionManagementViewModel(fakeRepository)

    // Then
    viewModel.uiState.test {
      assertIs<SubscriptionManagementUiState.Loading>(awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun whenOfferingFetchFails_stateIsError() = runTest {
    // Given
    val errorMessage = "Failed to fetch offering"
    fakeRepository.simulateOfferingError(errorMessage)

    // When
    val viewModel = SubscriptionManagementViewModel(fakeRepository)

    // Then
    viewModel.uiState.test {
      // Initial loading state
      assertIs<SubscriptionManagementUiState.Loading>(awaitItem())

      // Advance dispatcher to process the flow
      testDispatcher.scheduler.advanceUntilIdle()

      // Error state
      val errorState = awaitItem()
      assertIs<SubscriptionManagementUiState.Error>(errorState)
      assertEquals(errorMessage, errorState.message)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun whenCustomerInfoFetchFails_stateIsError() = runTest {
    // Given - offering succeeds but customer info fails
    // Note: We can't easily set a successful Offering since it's a RevenueCat SDK type,
    // so we test the error path for customer info by checking the combined error state
    val errorMessage = "Failed to fetch customer info"
    fakeRepository.simulateCustomerInfoError(errorMessage)
    // When offering also fails, the combined result shows an error
    // This test verifies the error handling path works correctly

    // When
    val viewModel = SubscriptionManagementViewModel(fakeRepository)

    // Then
    viewModel.uiState.test {
      // Initial loading state
      assertIs<SubscriptionManagementUiState.Loading>(awaitItem())

      // Advance dispatcher to process the flow
      testDispatcher.scheduler.advanceUntilIdle()

      // Error state - since both fail, we get an error
      val errorState = awaitItem()
      assertIs<SubscriptionManagementUiState.Error>(errorState)
      // The error message could be from either failure since offering is also not configured

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun whenBothOfferingAndCustomerInfoFail_stateIsError() = runTest {
    // Given
    val offeringError = "Offering error"
    val customerInfoError = "Customer info error"
    fakeRepository.simulateOfferingError(offeringError)
    fakeRepository.simulateCustomerInfoError(customerInfoError)

    // When
    val viewModel = SubscriptionManagementViewModel(fakeRepository)

    // Then
    viewModel.uiState.test {
      // Initial loading state
      assertIs<SubscriptionManagementUiState.Loading>(awaitItem())

      // Advance dispatcher to process the flow
      testDispatcher.scheduler.advanceUntilIdle()

      // Error state - should show offering error first based on combine logic
      val errorState = awaitItem()
      assertIs<SubscriptionManagementUiState.Error>(errorState)
      // The error could be either one based on combine order
      assert(errorState.message == offeringError || errorState.message == customerInfoError)

      cancelAndIgnoreRemainingEvents()
    }
  }
}
