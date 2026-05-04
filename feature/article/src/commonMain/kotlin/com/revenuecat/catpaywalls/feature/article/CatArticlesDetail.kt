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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.catpaywalls.core.designsystem.component.CatArticlesAppBar
import com.revenuecat.catpaywalls.core.designsystem.component.CatArticlesCircularProgress
import com.revenuecat.catpaywalls.core.designsystem.component.fadingEdge
import com.revenuecat.catpaywalls.core.designsystem.theme.CatArticlesTheme
import com.revenuecat.catpaywalls.core.model.Article
import com.revenuecat.catpaywalls.core.navigation.CatArticlesScreen
import com.revenuecat.catpaywalls.core.navigation.currentComposeNavigator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

private const val ENTITLEMENT_PREMIUM = "premium"
private const val FREE_DAILY_QUOTA = 3

@Composable
fun CatArticlesDetail(viewModel: CatArticlesDetailViewModel) {
  val composeNavigator = currentComposeNavigator
  val article by viewModel.article.collectAsState()

  if (article == null) {
    Box(modifier = Modifier.fillMaxSize()) {
      CatArticlesCircularProgress()
    }
    return
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(bottom = 80.dp)
      .verticalScroll(state = rememberScrollState()),
  ) {
    CatArticlesDetailContent(
      article = article!!,
      viewModel = viewModel,
      navigateUp = { composeNavigator.navigateUp() },
      navigateToPaywalls = { composeNavigator.navigate(CatArticlesScreen.Paywalls) },
    )
  }
}

@Composable
private fun CatArticlesDetailContent(
  article: Article,
  viewModel: CatArticlesDetailViewModel,
  navigateUp: () -> Unit,
  navigateToPaywalls: () -> Unit,
) {
  val customerInfo by viewModel.customerInfo.collectAsState()
  val bookmarkedTitles by viewModel.bookmarkedTitles.collectAsState()
  val todayReadCount by viewModel.todayReadCount.collectAsState()
  val shouldShowPromo by viewModel.shouldShowPromo.collectAsState()
  val offering by viewModel.offering.collectAsState()
  val isEntitled = customerInfo?.entitlements?.get(ENTITLEMENT_PREMIUM)?.isActive == true
  val isBookmarked = article.title in bookmarkedTitles
  val isQuotaExceeded = todayReadCount > FREE_DAILY_QUOTA && !isEntitled

  LaunchedEffect(article.title) {
    viewModel.recordRead(article.title)
  }

  if (shouldShowPromo) {
    offering?.let { current ->
      PromoBottomSheet(
        offering = current,
        onPurchase = { pkg -> viewModel.purchasePackage(pkg.identifier) },
        onDismiss = { viewModel.dismissPromo() },
      )
    }
  }

  DetailsAppBar(
    article = article,
    navigateUp = navigateUp,
    isBookmarked = isBookmarked,
    onToggleBookmark = {
      viewModel.toggleBookmark(
        articleTitle = article.title,
        isPremium = isEntitled,
        onNotPremium = navigateToPaywalls,
      )
    },
  )

  DetailsHeader(article = article)

  DetailsContent(
    article = article,
    onJoinClicked = navigateToPaywalls,
    isEntitled = isEntitled,
    isQuotaExceeded = isQuotaExceeded,
  )
}

@Composable
private fun DetailsAppBar(
  article: Article,
  navigateUp: () -> Unit,
  isBookmarked: Boolean,
  onToggleBookmark: () -> Unit,
) {
  CatArticlesAppBar(
    modifier = Modifier.background(CatArticlesTheme.colors.primary),
    title = article.title,
    navigationIcon = {
      Icon(
        modifier = Modifier.clickable(onClick = { navigateUp.invoke() }),
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        tint = Color.White,
        contentDescription = null,
      )
    },
    actions = {
      IconButton(onClick = onToggleBookmark) {
        Icon(
          imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
          contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
          tint = Color.White,
        )
      }
    },
  )
}

@Composable
private fun DetailsHeader(article: Article) {
  Column(
    modifier = Modifier.fillMaxWidth(),
  ) {
    LandscapistImage(
      modifier = Modifier
        .fillMaxWidth()
        .height(460.dp),
      imageModel = { article.cover },
      imageOptions = ImageOptions(contentScale = ContentScale.Crop),
      component = rememberImageComponent {
        +ShimmerPlugin(
          Shimmer.Resonate(
            baseColor = Color.Transparent,
            highlightColor = Color.LightGray,
          ),
        )
      },
    )
  }
}

@Composable
private fun DetailsContent(
  article: Article,
  isEntitled: Boolean,
  isQuotaExceeded: Boolean,
  onJoinClicked: () -> Unit,
) {
  if (isQuotaExceeded) {
    DailyLimitContent(onJoinClicked = onJoinClicked)
    return
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp)
      .fadingEdge(isEnabled = !isEntitled),
  ) {
    Text(
      text = article.title,
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp,
      color = CatArticlesTheme.colors.black,
    )

    Text(
      modifier = Modifier.padding(top = 4.dp),
      text = "${article.author} • ${article.date}",
      fontSize = 14.sp,
      color = CatArticlesTheme.colors.black,
    )

    Text(
      modifier = Modifier.padding(top = 16.dp),
      text = article.content + article.content,
      fontSize = 18.sp,
      color = CatArticlesTheme.colors.black,
    )
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(250.dp)
      .padding(vertical = 4.dp, horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
      text = "Want to read more?",
      fontWeight = FontWeight.Medium,
      fontSize = 20.sp,
      color = CatArticlesTheme.colors.black,
    )

    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
      textAlign = TextAlign.Center,
      text = "Subscribe to unlock unlimited access to all articles.",
      fontSize = 16.sp,
      color = CatArticlesTheme.colors.black,
    )

    Button(
      modifier = Modifier.padding(top = 40.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CatArticlesTheme.colors.primary,
      ),
      onClick = onJoinClicked,
    ) {
      Text(
        text = "Join Now",
        color = CatArticlesTheme.colors.absoluteWhite,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
      )
    }
  }
}

@Composable
private fun DailyLimitContent(onJoinClicked: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp, horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
      text = "Daily limit reached",
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
      color = CatArticlesTheme.colors.black,
    )

    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp),
      textAlign = TextAlign.Center,
      text = "You've used all $FREE_DAILY_QUOTA free articles for today. Subscribe for unlimited access.",
      fontSize = 16.sp,
      color = CatArticlesTheme.colors.black,
    )

    Button(
      modifier = Modifier.padding(top = 32.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CatArticlesTheme.colors.primary,
      ),
      onClick = onJoinClicked,
    ) {
      Text(
        text = "Join Now",
        color = CatArticlesTheme.colors.absoluteWhite,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
      )
    }
  }
}
