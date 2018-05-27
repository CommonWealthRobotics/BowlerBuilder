description = "The BowlerBuilder application."

spotless {
    java {
        licenseHeaderFile("${rootProject.rootDir}/config/spotless/bowlerbuilder.license", "(package|import)")
    }
    kotlin {
        licenseHeaderFile("${rootProject.rootDir}/config/spotless/bowlerbuilder.license", "(package|import)")
    }
}

checkstyle {
    configFile = file("${rootProject.rootDir}/config/checkstyle/checkstyle.xml")
}
