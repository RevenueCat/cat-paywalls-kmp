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

import com.revenuecat.catpaywalls.core.data.PaywallsRepository
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake implementation of [PaywallsRepository] for testing.
 */
class FakePaywallsRepository : PaywallsRepository {
  private var offeringResult: Result<Offering>? = null
  private var customerInfoResult: Result<CustomerInfo>? = null
  private var purchaseResults: MutableMap<String, Result<StoreTransaction>> = mutableMapOf()

  fun setOfferingResult(result: Result<Offering>?) {
    offeringResult = result
  }

  fun setCustomerInfoResult(result: Result<CustomerInfo>?) {
    customerInfoResult = result
  }

  fun setPurchaseResult(packageId: String, result: Result<StoreTransaction>) {
    purchaseResults[packageId] = result
  }

  fun simulateOfferingError(message: String = "Failed to fetch offering") {
    offeringResult = Result.failure(Exception(message))
  }

  fun simulateCustomerInfoError(message: String = "Failed to fetch customer info") {
    customerInfoResult = Result.failure(Exception(message))
  }

  override fun fetchOffering(): Flow<Result<Offering>> = flow {
    val result = offeringResult
      ?: Result.failure(IllegalStateException("No offering configured"))
    emit(result)
  }

  override fun fetchCustomerInfo(): Flow<Result<CustomerInfo>> = flow {
    val result = customerInfoResult
      ?: Result.failure(IllegalStateException("No customer info configured"))
    emit(result)
  }

  override fun awaitPurchase(packageId: String): Flow<Result<StoreTransaction>> = flow {
    val result = purchaseResults[packageId]
      ?: Result.failure(IllegalStateException("Package not found: $packageId"))
    emit(result)
  }
}
