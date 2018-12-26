plugins {
    `java-library`
}

description = "The core library."

repositories {
    maven(url = "https://dl.bintray.com/commonwealthrobotics/maven-artifacts")
}

dependencies {
    api(group = "com.neuronrobotics", name = "BowlerScriptingKernel", version = "0.32.4") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
        exclude(group = "com.google.guava")
    }

    api(
        group = "com.neuronrobotics",
        name = "kinematicschef-core",
        version = "0.0.14"
    ) {
        exclude(group = "org.slf4j", module = "slf4j-simple")
        exclude(group = "com.google.guava")
    }

    implementation(group = "org.apache.ivy", name = "ivy", version = "2.2.0")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(
        group = "com.google.inject.extensions",
        name = "guice-assistedinject",
        version = "4.1.0"
    )
}
