import KotlinUI_gradle.Versions.arrow_version
import KotlinUI_gradle.Versions.kernel_version

plugins {
    `java-library`
    application
}

application.mainClassName = "com.neuronrobotics.bowlerbuilder.BowlerBuilder"

object Versions {
    const val arrow_version = "0.8.1"
    const val kernel_version = "0.0.17"
}

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/staging/")
    maven(url = "https://jitpack.io")
    maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
    jcenter()
}

dependencies {
    api(group = "com.neuronrobotics", name = "bowler-kernel-kinematics", version = kernel_version)
    api(group = "com.neuronrobotics", name = "bowler-cad-core", version = "0.0.5")
    api(group = "com.neuronrobotics", name = "java-bowler", version = "3.26.2")
    api(group = "io.arrow-kt", name = "arrow-core", version = arrow_version)

    implementation(
        group = "com.neuronrobotics",
        name = "bowler-kernel-config",
        version = kernel_version
    )
    implementation(group = "org.octogonapus", name = "kt-guava-core", version = "0.0.5")

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
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-grapher",
        version = "4.1.0"
    )
    implementation(group = "org.jlleitschuh.guice", name = "kotlin-guiced-core", version = "0.0.5")
    implementation(group = "org.greenrobot", name = "eventbus", version = "3.1.1")
    implementation(group = "org.apache.commons", name = "commons-text", version = "1.2")
    implementation(group = "commons-io", name = "commons-io", version = "2.6")
    implementation(group = "commons-validator", name = "commons-validator", version = "1.6")
    implementation(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
    implementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    implementation(group = "com.beust", name = "klaxon", version = "5.0.5")

    implementation(group = "org.bouncycastle", name = "bcprov-jdk15on", version = "1.60")
    implementation(group = "org.bouncycastle", name = "bcpg-jdk15on", version = "1.60")
    implementation(
        group = "name.neuhalfen.projects.crypto.bouncycastle.openpgp",
        name = "bouncy-gpg",
        version = "2.1.2"
    )
}
