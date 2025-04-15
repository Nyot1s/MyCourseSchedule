plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mycourseschedule"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mycourseschedule"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room dependencies
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Explicitly include JetBrains annotations to resolve conflict
    implementation(libs.jetbrains.annotations)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Optional: Add a constraint to force the newer annotations version
    constraints {
        implementation("org.jetbrains:annotations:23.0.0") {
            because("Avoid conflict with com.intellij:annotations:12.0")
        }
    }
}