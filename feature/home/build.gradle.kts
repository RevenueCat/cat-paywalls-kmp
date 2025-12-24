plugins {
  id("catpaywalls.kmp.feature")
}

android {
  namespace = "com.revenuecat.catpaywalls.feature.home"
}

kotlin {
  sourceSets {
    commonTest.dependencies {
      implementation(projects.core.data)
    }
  }
}
