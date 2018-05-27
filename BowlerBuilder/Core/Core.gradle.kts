description = "The core library."

plugins {
    id("java-library")
}

dependencies {
    api(project(":bowler-script-kernel"))
    implementation(group = "org.apache.ivy", name = "ivy", version = "2.2.0")
    implementation(group = "com.google.inject", name = "guice", version = "4.1.0")
    implementation(group = "com.google.inject.extensions", name = "guice-assistedinject", version = "4.1.0")
}
