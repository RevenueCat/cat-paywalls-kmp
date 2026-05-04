plugins {
  id("catpaywalls.kmp.library")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.revenuecat.catpaywalls.core.data"
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.core.model)
      implementation(projects.core.network)

      api(libs.purchases.kmp.core)
      api(libs.kotlinx.coroutines.core)

      api(libs.androidx.datastore.preferences.core)
      implementation(libs.kotlinx.datetime)
    }
  }
}
