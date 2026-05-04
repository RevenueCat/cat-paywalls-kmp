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

import com.revenuecat.catpaywalls.core.data.ReadingTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeReadingTrackerRepository : ReadingTrackerRepository {
  private val state = MutableStateFlow(0)

  override val todayReadCount: Flow<Int> = state

  override suspend fun recordArticleRead(articleTitle: String) {
    state.value += 1
  }

  fun setCount(value: Int) {
    state.value = value
  }
}
