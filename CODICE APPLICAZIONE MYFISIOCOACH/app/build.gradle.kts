plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.myfisiocoach"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myfisiocoach"
        minSdk = 27
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }



}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("io.github.ShawnLin013:number-picker:2.4.13")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("mysql:mysql-connector-java:5.1.49")
    implementation ("javax.activation:activation:1.1.1")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0") // Aggiungi il converter per le stringhe



    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.android.exoplayer:exoplayer:2.18.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("androidx.biometric:biometric:1.2.0-alpha04")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}