import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.example.listedecontactsv3"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.listedecontactsv3"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] =
            localProps.getProperty("MAPS_API_KEY", "CLE_MANQUANTE")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    dependencies {

        implementation("androidx.core:core-ktx:1.15.0")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("androidx.fragment:fragment-ktx:1.8.5")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("androidx.preference:preference-ktx:1.2.1")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("com.google.android.gms:play-services-maps:20.1.0")
    }
}