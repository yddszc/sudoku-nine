plugins {
    id("com.android.application")
}

android {
    namespace = "com.orange.sudoku"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.orange.sudoku"
        minSdk = 26          // Android 8.0 及以上（2017 年后机型）
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        // 纯离线单机游戏：不申请任何权限，不需要网络
    }

    // 个人自用固定签名：保证每次构建签名一致，新版本可直接覆盖安装、进度不丢
    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystore/sudoku.jks")
            storePassword = "sudoku123"
            keyAlias = "sudoku"
            keyPassword = "sudoku123"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
