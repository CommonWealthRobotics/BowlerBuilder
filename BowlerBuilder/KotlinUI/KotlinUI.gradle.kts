import KotlinUI_gradle.Verions.arrow_version

plugins {
    `java-library`
    application
    id("org.jetbrains.kotlin.kapt")
    idea
}

application.mainClassName = "com.neuronrobotics.bowlerbuilder.BowlerBuilder"

object Verions {
    const val arrow_version = "0.8.1"
}

repositories {
    maven(url = "https://dl.bintray.com/commonwealthrobotics/maven-artifacts")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":BowlerBuilder:Core")) {
        exclude(group = "com.google.guava")
    }
    implementation(group = "com.google.guava", name = "guava", version = "25.0-jre")
    implementation(group = "org.controlsfx", name = "controlsfx", version = "8.40.14")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
    implementation(group = "org.greenrobot", name = "eventbus", version = "3.1.1")
    implementation(group = "org.apache.commons", name = "commons-text", version = "1.2")
    implementation(group = "commons-io", name = "commons-io", version = "2.6")
    implementation(group = "commons-validator", name = "commons-validator", version = "1.6")
    implementation(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
    implementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    implementation(group = "com.beust", name = "klaxon", version = "3.0.1")
    implementation(group = "org.fxmisc.richtext", name = "richtextfx", version = "0.9.0")

    api(group = "io.arrow-kt", name = "arrow-core", version = arrow_version)
    kapt(group = "io.arrow-kt", name = "arrow-annotations-processor", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-syntax", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-data", version = arrow_version)
    api(group = "io.arrow-kt", name = "arrow-optics", version = arrow_version)
}

idea {
    module {
        sourceDirs = sourceDirs + files(
            "build/generated/source/kapt/main",
            "build/generated/source/kapt/debug",
            "build/generated/source/kapt/release",
            "build/generated/source/kaptKotlin/main",
            "build/generated/source/kaptKotlin/debug",
            "build/generated/source/kaptKotlin/release",
            "build/tmp/kapt/main/kotlinGenerated"
        )

        @Suppress("UnstableApiUsage")
        generatedSourceDirs = generatedSourceDirs + files(
            "build/generated/source/kapt/main",
            "build/generated/source/kapt/debug",
            "build/generated/source/kapt/release",
            "build/generated/source/kaptKotlin/main",
            "build/generated/source/kaptKotlin/debug",
            "build/generated/source/kaptKotlin/release",
            "build/tmp/kapt/main/kotlinGenerated"
        )
    }
}