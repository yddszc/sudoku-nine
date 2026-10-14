plugins {
    id("com.android.application")
}

// 仓库内的固定签名文件。存在就用它（每次构建签名一致，可覆盖升级、进度不丢）；
// 万一缺失，debug 会退回到 Android 默认 debug 签名，保证一定能出包。
val fixedKeystore = rootProject.file("keystore/sudoku.jks")

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

    signingConfigs {
        if (fixedKeystore.exists()) {
            create("fixed") {
                storeFile = fixedKeystore
                storePassword = "sudoku123"
                keyAlias = "sudoku"
                keyPassword = "sudoku123"
            }
        }
    }

    buildTypes {
        debug {
            // 默认即 debuggable = true、不混淆。
            // 有固定签名就用固定签名，没有就用 AGP 自带的调试签名，不会构建失败。
            if (fixedKeystore.exists()) {
                signingConfig = signingConfigs.getByName("fixed")
            }
        }
        release {
            isMinifyEnabled = false
            if (fixedKeystore.exists()) {
                signingConfig = signingConfigs.getByName("fixed")
            }
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
