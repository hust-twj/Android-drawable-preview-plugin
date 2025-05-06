// 插件版本和 Kotlin 版本定义
plugins {
    id("java")
    // IntelliJ 插件开发的 Gradle 插件
    id("org.jetbrains.intellij") version "1.13.3"
    //  Kotlin JVM 插件
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
}

group = "com.husttwj.drawablepreview"
//1. change version
//2. update changelog in plugin.xml
version = "2.1.0"


java {
    sourceCompatibility = JavaVersion.VERSION_17
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    // https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
    // https://plugins.jetbrains.com/docs/intellij/android-studio.html#android-studio-releases-listing
    version.set("2023.2.1.23")
    type.set("AI") // Target IDE Platform

//    https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html
    plugins.set(listOf("org.jetbrains.android"))

    /**
     * Patch plugin.xml with since and until build
     * values inferred from IDE version.
     */
    //updateSinceUntilBuild.set(false)

}

repositories {
    google()
    mavenCentral()
}

// 配置依赖项
dependencies {

    // Kotlin 标准库
   // implementation(kotlin("stdlib-jdk8"))

    //parse svga
  //  implementation("org.apache.xmlgraphics:batik-transcoder:1.9")

}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("")
    }

    buildSearchableOptions {
        enabled = false
    }

//    runIde {
//        ideDir.set(file("/Applications/Android Studio Preview.app/Contents"))
//    }
}

// 确保 Kotlin 目录也支持 Java 文件
sourceSets["main"].java.srcDirs("src/main/java", "src/main/kotlin")
