import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
  kotlin("native.cocoapods")
}

kotlin {
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  cocoapods {
    summary = "Cat Paywalls KMP App"
    homepage = "https://github.com/revenuecat/cat-paywalls-kmp"
    version = "1.0"
    ios.deploymentTarget = "15.0"
    podfile = project.file("../iosApp/Podfile")
    framework {
      baseName = "ComposeApp"
      isStatic = true
    }
    pod("RevenueCat") {
      version = "~> 5.71"
      extraOpts += listOf("-compiler-option", "-fmodules")
    }
    pod("RevenueCatUI") {
      version = "~> 5.71"
      extraOpts += listOf("-compiler-option", "-fmodules")
    }
  }

  sourceSets {
    commonMain.dependencies {
      // Core modules - use api() for modules with Metro bindings
      implementation(projects.core.model)
      api(projects.core.network)
      api(projects.core.data)
      implementation(projects.core.designsystem)
      implementation(projects.core.navigation)

      // Feature modules
      implementation(projects.feature.home)
      implementation(projects.feature.article)
      implementation(projects.feature.paywalls)
      implementation(projects.feature.account)
      implementation(projects.feature.subscriptions)
      implementation(projects.feature.bookmarks)

      // Compose
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)

      // Navigation
      implementation(libs.navigation.compose)

      // Coroutines
      implementation(libs.kotlinx.coroutines.core)

      // Serialization
      implementation(libs.kotlinx.serialization.json)

      // RevenueCat
      implementation(libs.purchases.kmp.core)

      // Image loading (Landscapist)
      implementation(libs.landscapist.image)
      implementation(libs.landscapist.placeholder)
      implementation(libs.landscapist.zoomable)

      // Lifecycle (JetBrains KMP)
      implementation(libs.jetbrains.lifecycle.viewmodel)
      implementation(libs.jetbrains.lifecycle.viewmodel.compose)
    }

    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)
      implementation(libs.kotlinx.coroutines.android)
      implementation(libs.ktor.client.okhttp)
    }

    iosMain.dependencies {
      implementation(libs.ktor.client.darwin)
    }
  }
}

val localProperties = Properties().apply {
  val file = rootProject.file("local.properties")
  if (file.exists()) file.inputStream().use { load(it) }
}

android {
  namespace = "com.revenuecat.catpaywalls"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.revenuecat.catpaywalls"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"

    // RevenueCat Test Store API key (loaded from local.properties)
    buildConfigField(
      "String",
      "REVENUECAT_TEST_API_KEY",
      "\"${localProperties.getProperty("revenuecat.test.api.key", "")}\"",
    )
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}
