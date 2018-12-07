description = "The BowlerBuilder application."

checkstyle {
    configFile = file("${rootProject.rootDir}/config/checkstyle/checkstyle.xml")
}

repositories {
    maven(url = "https://dl.bintray.com/commonwealthrobotics/maven-artifacts")
}
