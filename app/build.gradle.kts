plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "root.dongmin.eat_da"
    compileSdk = 35


    defaultConfig {
        applicationId = "root.dongmin.eat_da"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {

        viewBinding = true

    }
    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

//configurations.all {
//    resolutionStrategy {
//        force("com.google.protobuf:protobuf-javalite:4.26.1")
//    }
//}



dependencies {



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.appdistribution.gradle)

    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.3.0")

     // 위치기반
    implementation ("com.google.android.gms:play-services-location:21.3.0")


    implementation("com.android.volley:volley:1.2.1")

    //서버
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation ("androidx.recyclerview:recyclerview:1.4.0")


    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation ("com.kakao.maps.open:android:2.11.0")


    implementation("com.google.android.material:material:1.12.0")

    implementation ("com.google.code.gson:gson:2.8.8")

    implementation ("com.google.android.flexbox:flexbox:3.0.0") //FlexboxLayoutManager 추가임
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation ("com.google.guava:guava:32.1.2-android") // 최신 버전 (2024년 기준)

    //implementation("com.google.mediapipe:tasks-genai:0.10.20")

}