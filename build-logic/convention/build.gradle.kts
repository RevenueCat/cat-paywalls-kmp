import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
}

group = "com.revenuecat.catpaywalls.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
  }
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.compose.gradlePlugin)
  runtimeOnly(libs.metro.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("kmpLibrary") {
      id = "catpaywalls.kmp.library"
      implementationClass = "KmpLibraryConventionPlugin"
    }
    register("kmpLibraryCompose") {
      id = "catpaywalls.kmp.library.compose"
      implementationClass = "KmpLibraryComposeConventionPlugin"
    }
    register("kmpFeature") {
      id = "catpaywalls.kmp.feature"
      implementationClass = "KmpFeatureConventionPlugin"
    }
  }
}
