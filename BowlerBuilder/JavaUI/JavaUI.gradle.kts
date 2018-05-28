plugins {
    application
}

application {
    mainClassName = "com.neuronrobotics.bowlerbuilder.BowlerBuilder"
}

dependencies {
    compile(group = "org.controlsfx", name = "controlsfx", version = "8.40.14")
    compile(group = "com.google.guava", name = "guava", version = "25.0-jre")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(group = "com.google.inject.extensions", name = "guice-assistedinject", version = "4.1.0")
    compile(group = "org.apache.commons", name = "commons-text", version = "1.2")
    compile(group = "commons-io", name = "commons-io", version = "2.6")
    compile(group = "commons-validator", name = "commons-validator", version = "1.6")
    compile(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1")
    compile(group = "com.natpryce", name = "hamkrest", version = "1.4.2.2")
    implementation(group = "com.beust", name = "klaxon", version = "3.0.1")
    compile(project(":BowlerBuilder:Core"))
}
