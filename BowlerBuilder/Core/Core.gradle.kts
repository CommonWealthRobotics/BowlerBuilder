plugins {
    `java-library`
}

description = "The core library."

dependencies {
    // api(project(":bowler-script-kernel")) {
    api(group = "com.neuronrobotics", name = "BowlerScriptingKernel", version = "0.32.4") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }

    implementation(group = "org.apache.ivy", name = "ivy", version = "2.2.0")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(group = "com.google.inject.extensions", name = "guice-assistedinject", version = "4.1.0")
}
