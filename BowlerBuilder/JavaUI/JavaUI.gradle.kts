plugins {
    `java-library`
    application
}

application.mainClassName = "com.neuronrobotics.bowlerbuilder.BowlerBuilder"

dependencies {
    implementation(project(":BowlerBuilder:Core")) {
        exclude(group = "com.google.guava")
    }
    implementation(group = "com.google.guava", name = "guava", version = "25.0-jre")
    implementation(group = "org.controlsfx", name = "controlsfx", version = "8.40.14")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(group = "com.google.inject.extensions", name = "guice-assistedinject", version = "4.1.0")
    implementation(group = "org.apache.commons", name = "commons-text", version = "1.2")
    implementation(group = "commons-io", name = "commons-io", version = "2.6")
    implementation(group = "commons-validator", name = "commons-validator", version = "1.6")
    implementation(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
    implementation(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    implementation(group = "com.beust", name = "klaxon", version = "3.0.1")
    implementation(group = "org.fxmisc.richtext", name = "richtextfx", version = "0.9.0")
}
