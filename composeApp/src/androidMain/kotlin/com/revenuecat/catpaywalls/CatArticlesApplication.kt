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
package com.revenuecat.catpaywalls

import android.app.Application
import com.revenuecat.catpaywalls.core.data.di.installApplicationContext
import com.revenuecat.catpaywalls.di.AppGraph
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import dev.zacsweers.metro.createGraph

class CatArticlesApplication : Application() {
  lateinit var appGraph: AppGraph
    private set

  override fun onCreate() {
    super.onCreate()

    installApplicationContext(this)
    appGraph = createGraph<AppGraph>()

    // Initialize RevenueCat. When BuildConfig.REVENUECAT_TEST_API_KEY is set in
    // local.properties (revenuecat.test.api.key=...), the Test Store sandbox key
    // takes precedence so the app exercises the Test Store purchase flow.
    Purchases.logLevel = LogLevel.DEBUG
    val apiKey = BuildConfig.REVENUECAT_TEST_API_KEY.takeIf { it.isNotBlank() } ?: REVENUECAT_API_KEY
    Purchases.configure(
      PurchasesConfiguration(apiKey = apiKey) {
        appUserId = null // Anonymous user
      },
    )
  }

  companion object {
    // Replace with your actual RevenueCat API key
    private const val REVENUECAT_API_KEY = "your_revenuecat_api_key"
  }
}
