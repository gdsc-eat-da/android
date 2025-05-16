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

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/io.netty.versions.properties",
                "google/protobuf/field_mask.proto",
                "google/protobuf/type.proto"
            )
        }
    }
}

dependencies {
    // Android 기본 구성
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation(libs.firebase.appdistribution.gradle)

    // Google Play 서비스
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // 네트워크
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // UI
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.kakao.maps.open:android:2.11.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // 유틸
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:32.1.2-android")

    //gemma
    implementation ("com.google.mediapipe:tasks-genai:0.10.5")

    // AI/ML 관련
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }

    implementation("com.google.mediapipe:tasks-genai:0.10.20") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }


    // gRPC 사용 시 (protobuf-java 제거 필수)
    implementation("io.grpc:grpc-protobuf:1.69.1") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }

    // 명시적으로 javalite 추가
    implementation("com.google.protobuf:protobuf-javalite:4.26.1")

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

configurations.all {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    resolutionStrategy {
        // 버전 충돌 방지
        force("com.google.code.gson:gson:2.10.1")
        force("com.squareup.okhttp3:okhttp:4.12.0")
        force("com.google.guava:guava:32.1.2-android")
        force("com.google.protobuf:protobuf-javalite:4.26.1")
    }
}
