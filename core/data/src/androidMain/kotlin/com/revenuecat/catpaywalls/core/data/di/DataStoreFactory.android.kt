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
package com.revenuecat.catpaywalls.core.data.di

import android.content.Context
import okio.Path
import okio.Path.Companion.toOkioPath

private lateinit var applicationContext: Context

/** Must be called once from `Application.onCreate()` before the DI graph is created. */
fun installApplicationContext(context: Context) {
  applicationContext = context.applicationContext
}

internal actual fun dataStoreDirectory(): Path {
  check(::applicationContext.isInitialized) {
    "Application context not installed. Call installApplicationContext(this) in Application.onCreate()."
  }
  return applicationContext.filesDir.toOkioPath().resolve("datastore")
}
