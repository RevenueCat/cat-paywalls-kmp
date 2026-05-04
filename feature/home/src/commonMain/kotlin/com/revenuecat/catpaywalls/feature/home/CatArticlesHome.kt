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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.catpaywalls.core.designsystem.component.CatArticlesAppBar
import com.revenuecat.catpaywalls.core.designsystem.component.CatArticlesCircularProgress
import com.revenuecat.catpaywalls.core.designsystem.theme.CatArticlesTheme
import com.revenuecat.catpaywalls.core.model.Article
import com.revenuecat.catpaywalls.core.navigation.CatArticlesScreen
import com.revenuecat.catpaywalls.core.navigation.currentComposeNavigator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun CatArticlesHome(viewModel: CatArticlesViewModel) {
  val uiState by viewModel.uiState.collectAsState()
  val composeNavigator = currentComposeNavigator

  Column(modifier = Modifier.fillMaxSize()) {
    CatArticlesAppBar(
      modifier = Modifier.background(CatArticlesTheme.colors.primary),
      actions = {
        IconButton(onClick = { composeNavigator.navigate(CatArticlesScreen.Account) }) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Account",
            tint = Color.White,
          )
        }
      },
    )

    HomeContent(
      uiState = uiState,
      onNavigateToDetails = { composeNavigator.navigate(CatArticlesScreen.CatArticle(articleId = it.id)) },
    )
  }
}

@Composable
private fun HomeContent(uiState: HomeUiState, onNavigateToDetails: (Article) -> Unit) {
  Box(modifier = Modifier.fillMaxSize()) {
    when (uiState) {
      is HomeUiState.Loading -> {
        CatArticlesCircularProgress()
      }

      is HomeUiState.Success -> {
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          contentPadding = PaddingValues(6.dp),
        ) {
          items(items = uiState.articles, key = { it.title }) { article ->
            ArticleCard(
              article = article,
              onNavigateToDetails = onNavigateToDetails,
            )
          }
        }
      }

      is HomeUiState.Error -> {
        Text(
          text = uiState.message ?: "Unknown error",
          modifier = Modifier.align(Alignment.Center),
          color = CatArticlesTheme.colors.white,
        )
      }
    }
  }
}

@Composable
private fun ArticleCard(article: Article, onNavigateToDetails: (Article) -> Unit) {
  Box(
    modifier = Modifier
      .padding(8.dp)
      .fillMaxWidth()
      .height(300.dp)
      .clip(RoundedCornerShape(6.dp))
      .clickable { onNavigateToDetails.invoke(article) },
  ) {
    LandscapistImage(
      modifier = Modifier.fillMaxSize(),
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

    Text(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .background(Color.Black.copy(alpha = 0.65f))
        .padding(12.dp),
      text = article.title,
      color = Color.White,
      textAlign = TextAlign.Center,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
    )
  }
}
