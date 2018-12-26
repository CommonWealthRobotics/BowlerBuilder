

plugins {
    `java-library`
    application
}

application.mainClassName = "com.neuronrobotics.bowlerbuilder.BowlerBuilder"

object Versions {
    const val arrow_version = "0.8.1"
}

repositories {
    maven(url = "https://dl.bintray.com/commonwealthrobotics/maven-artifacts")
    maven(url = "https://jitpack.io")
}

dependencies {
    api(project(":BowlerKernel:Core"))
    api(group = "com.neuronrobotics", name = "BowlerScriptingKernel", version = "0.34.1") {
        exclude(group = "org.slf4j")
        exclude(group = "com.google.guava")
        exclude(group = "org.kohsuke")
        exclude(group = "org.eclipse.jgit")
    }

    implementation(
        group = "org.eclipse.jgit",
        name = "org.eclipse.jgit",
        version = "5.2.0.201812061821-r"
    )

    implementation(group = "org.jsoup", name = "jsoup", version = "1.11.3")
    implementation(group = "com.google.guava", name = "guava", version = "27.0.1-jre")
    implementation(group = "org.controlsfx", name = "controlsfx", version = "8.40.14")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
    implementation(group = "org.jlleitschuh.guice", name = "kotlin-guiced-core", version = "0.0.5")
    implementation(group = "org.greenrobot", name = "eventbus", version = "3.1.1")
    implementation(group = "org.apache.commons", name = "commons-text", version = "1.2")
    implementation(group = "commons-io", name = "commons-io", version = "2.6")
    implementation(group = "commons-validator", name = "commons-validator", version = "1.6")
    implementation(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
    implementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    implementation(group = "com.beust", name = "klaxon", version = "3.0.1")
}
