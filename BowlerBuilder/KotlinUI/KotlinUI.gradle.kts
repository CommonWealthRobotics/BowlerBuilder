plugins {
    `java-library`
    application
}

val kernel_version = "0.0.26"

fun DependencyHandler.arrow(name: String) =
    create(group = "io.arrow-kt", name = name, version = property("arrow.version") as String)

application {
    mainClassName = "com.neuronrobotics.bowlerbuilder.BowlerBuilder"
    applicationDefaultJvmArgs += listOf(
        // For various usages of TraversalEngine
        "--add-exports=javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED",
        "--add-exports=javafx.graphics/com.sun.javafx.scene.traversal=ALL-UNNAMED",
        // For FontAwesome Style
        "--add-exports=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED",
        // For various behaviors across controls
        "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED",
        // For ReadOnlyUnbackedObservableList across files
        "--add-exports=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED",
        // For InputMap used in behavior classes
        "--add-exports=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED",
        // For EventHandlerManager in AutoCompletionBinding
        "--add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED",
        // For MappingChange, NonIterableChange across files
        "--add-exports=javafx.base/com.sun.javafx.collections=ALL-UNNAMED",
        // For VersionInfo in VersionChecker
        "--add-exports=javafx.base/com.sun.javafx.runtime=ALL-UNNAMED",
        // For WebPage in SVGLoader
        "--add-exports=javafx.web/com.sun.webkit=ALL-UNNAMED",
        // For enabling FontAwesome
        "--add-exports=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED"
        // TODO: Remove this option when Guice is moved to ByteBuddy https://github.com/google/guice/issues/1133
//        "--add-opens java.base/java.lang=ALL-UNNAMED"
    )
}

tasks.withType<CreateStartScripts> {
    (windowsStartScriptGenerator as TemplateBasedScriptGenerator).template =
        resources.text.fromFile("${rootProject.rootDir}/config/windowsStartScriptTemplate.txt")
}

repositories {
    maven(url = "https://oss.sonatype.org/content/repositories/staging/")
    maven(url = "https://jitpack.io")
    maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
    jcenter()
}

dependencies {
    api(group = "com.neuronrobotics", name = "bowler-kernel-kinematics", version = kernel_version)
    api(group = "com.neuronrobotics", name = "bowler-cad-core", version = "0.0.9")
    api(group = "com.neuronrobotics", name = "java-bowler", version = "3.26.2")
    api(group = "org.octogonapus", name = "kt-guava-core", version = "0.2.1")

    implementation(arrow("arrow-core-data"))
    implementation(arrow("arrow-core-extensions"))
    implementation(arrow("arrow-syntax"))
    implementation(arrow("arrow-typeclasses"))
    implementation(arrow("arrow-extras-data"))
    implementation(arrow("arrow-extras-extensions"))

    implementation(
        group = "com.neuronrobotics",
        name = "bowler-kernel-config",
        version = kernel_version
    )

    implementation(
        group = "org.eclipse.jgit",
        name = "org.eclipse.jgit",
        version = "5.2.0.201812061821-r"
    )
    implementation(group = "org.jsoup", name = "jsoup", version = "1.11.3")
    implementation(group = "com.google.guava", name = "guava", version = "27.0.1-jre")
    implementation(group = "org.controlsfx", name = "controlsfx", version = "11.0.0")
    implementation(group = "com.google.inject", name = "guice", version = "4.2.2")
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
