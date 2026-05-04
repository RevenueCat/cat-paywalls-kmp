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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpFeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("catpaywalls.kmp.library.compose")
        apply("org.jetbrains.kotlin.plugin.serialization")
        apply("dev.zacsweers.metro")
      }

      val libs =
        extensions
          .getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
          .named("libs")
      val compose = extensions.getByType<ComposeExtension>()

      extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
          commonMain.dependencies {
            // Core modules
            implementation(project(":core:model"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:navigation"))
            implementation(project(":core:data"))

            // Compose & Navigation
            implementation(compose.dependencies.runtime)
            implementation(compose.dependencies.foundation)
            implementation(compose.dependencies.material3)
            implementation(compose.dependencies.materialIconsExtended)

            // Navigation
            implementation(libs.findLibrary("navigation-compose").get())

            // Serialization
            implementation(libs.findLibrary("kotlinx-serialization-json").get())

            // Image loading (Landscapist)
            implementation(libs.findLibrary("landscapist-image").get())
            implementation(libs.findLibrary("landscapist-placeholder").get())
            implementation(libs.findLibrary("landscapist-zoomable").get())
          }
        }
      }
    }
  }
}
