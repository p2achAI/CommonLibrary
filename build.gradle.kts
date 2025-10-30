plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("maven-publish")
}

android {
    namespace = "ai.p2ach.commonlibrary"
    compileSdk = 36
    defaultConfig { minSdk = 24 }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    // ✅ 퍼블리시할 variant 명시
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

// ✅ 반드시 afterEvaluate 안에서 components["release"] 접근
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.seehoon"
                artifactId = "commonLibrary"
                // version은 JitPack이 자동으로 git tag 사용
            }
        }
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.fragment)

    implementation(libs.orhanobut.logger)
}