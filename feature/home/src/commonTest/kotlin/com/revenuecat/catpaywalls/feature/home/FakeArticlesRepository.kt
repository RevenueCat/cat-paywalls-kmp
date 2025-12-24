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

import com.revenuecat.catpaywalls.core.data.ArticlesRepository
import com.revenuecat.catpaywalls.core.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake implementation of [ArticlesRepository] for testing.
 */
class FakeArticlesRepository : ArticlesRepository {
  private var articlesResult: Result<List<Article>> = Result.success(emptyList())
  private var articleByIdResults: MutableMap<Long, Result<Article>> = mutableMapOf()

  fun setArticlesResult(result: Result<List<Article>>) {
    articlesResult = result
  }

  fun setArticleByIdResult(id: Long, result: Result<Article>) {
    articleByIdResults[id] = result
  }

  override fun fetchArticles(): Flow<Result<List<Article>>> = flow {
    emit(articlesResult)
  }

  override fun getArticleById(id: Long): Flow<Result<Article>> = flow {
    val result = articleByIdResults[id]
      ?: Result.failure(NoSuchElementException("Article with id $id not found"))
    emit(result)
  }

  companion object {
    fun createSampleArticles(count: Int = 3): List<Article> = (1..count).map { index ->
      Article(
        title = "Test Article $index",
        content = "This is the content for test article $index",
        description = "Description for article $index",
        author = "Test Author $index",
        date = "2025-01-0$index",
        cover = "https://example.com/cover$index.jpg",
      )
    }
  }
}
