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
package com.revenuecat.catpaywalls.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.revenuecat.catpaywalls.core.navigation.CatArticlesNavigatorImpl
import com.revenuecat.catpaywalls.core.navigation.CatArticlesScreen
import com.revenuecat.catpaywalls.core.navigation.LocalComposeNavigator
import com.revenuecat.catpaywalls.di.AppGraph
import com.revenuecat.catpaywalls.feature.account.AccountScreen
import com.revenuecat.catpaywalls.feature.article.CatArticlesDetail
import com.revenuecat.catpaywalls.feature.bookmarks.BookmarksScreen
import com.revenuecat.catpaywalls.feature.home.CatArticlesHome
import com.revenuecat.catpaywalls.feature.paywalls.CatCustomPaywalls
import com.revenuecat.catpaywalls.feature.subscriptions.SubscriptionManagementScreen

@Composable
fun CatArticlesNavHost(appGraph: AppGraph) {
  val navController = rememberNavController()
  val navigator = remember(navController) { CatArticlesNavigatorImpl(navController) }

  CompositionLocalProvider(
    LocalComposeNavigator provides navigator,
  ) {
    NavHost(
      navController = navController,
      startDestination = CatArticlesScreen.CatHome,
    ) {
      composable<CatArticlesScreen.CatHome> {
        CatArticlesHome(viewModel = appGraph.catArticlesViewModel)
      }

      composable<CatArticlesScreen.CatArticle> { backStackEntry ->
        val screen = backStackEntry.toRoute<CatArticlesScreen.CatArticle>()
        val articleDetailViewModel = remember(screen.articleId) {
          appGraph.articleDetailViewModelFactory.create(screen.articleId)
        }
        CatArticlesDetail(viewModel = articleDetailViewModel)
      }

      composable<CatArticlesScreen.Paywalls> {
        CatCustomPaywalls()
      }

      composable<CatArticlesScreen.Account> {
        AccountScreen()
      }

      composable<CatArticlesScreen.SubscriptionManagement> {
        SubscriptionManagementScreen(viewModel = appGraph.subscriptionManagementViewModel)
      }

      composable<CatArticlesScreen.Bookmarks> {
        BookmarksScreen(viewModel = appGraph.bookmarksViewModel)
      }
    }
  }
}
