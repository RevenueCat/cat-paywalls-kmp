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
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("org.jetbrains.kotlin.multiplatform")
        apply("com.android.library")
      }

      extensions.configure<KotlinMultiplatformExtension> {
        androidTarget {
          compilations.all {
            compileTaskProvider.configure {
              compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
              }
            }
          }
        }

        listOf(
          iosX64(),
          iosArm64(),
          iosSimulatorArm64(),
        ).forEach { iosTarget ->
          iosTarget.binaries.framework {
            baseName = project.name
            isStatic = true
          }
        }

        sourceSets.apply {
          commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
          }

          commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
            implementation(libs.findLibrary("turbine").get())
          }
        }
      }

      extensions.configure<LibraryExtension> {
        val catalog =
          extensions
            .getByType(
              org.gradle.api.artifacts.VersionCatalogsExtension::class.java,
            ).named("libs")

        namespace = "com.revenuecat.catpaywalls.${project.name.replace("-", ".")}"
        compileSdk =
          catalog
            .findVersion("android-compileSdk")
            .get()
            .toString()
            .toInt()

        defaultConfig {
          minSdk =
            catalog
              .findVersion("android-minSdk")
              .get()
              .toString()
              .toInt()
        }

        compileOptions {
          sourceCompatibility = JavaVersion.VERSION_11
          targetCompatibility = JavaVersion.VERSION_11
        }
      }
    }
  }
}

private val Project.libs
  get() =
    extensions
      .getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
      .named("libs")
