import java.util.Properties
import java.io.FileInputStream

plugins {
    application
}

application {
    mainClassName = "com.neuronrobotics.bowlerstudio.BowlerKernel"
}

repositories {
    maven("https://repository-bubblecloud.forge.cloudbees.com/release/")
    maven("https://clojars.org/repo")
    maven("https://oss.sonatype.org/content/repositories/releases/")
    maven("https://jline.sourceforge.net/m2repo")
    maven("https://repo.spring.io/milestone")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
    maven("https://jenkinsci.artifactoryonline.com/jenkinsci/public/")
}

fun getOSName() = System.getProperty("os.name")

fun getOSArch() = System.getProperty("os.arch")

fun is64Bit() = getOSArch().run {
    this.startsWith("x86_64") || this.startsWith("amd64")
}

fun isARM() = getOSArch().startsWith("arm")

fun isWindows() = getOSName().toLowerCase().run {
    this.startsWith("windows") || this.startsWith("microsoft") || this.startsWith("ms")
}

fun isLinux() = getOSName().toLowerCase().startsWith("linux")

fun isOSX() = getOSName().toLowerCase().startsWith("mac")

val props = Properties().also {
    it.load(FileInputStream(projectDir.absolutePath + "/src/main/resources/com/neuronrobotics/bowlerkernel/build.properties"))
}

dependencies {
    compile(group = "gov.nist.math", name = "jama", version = "1.0.2")
    compile(group = "org.reactfx", name = "reactfx", version = "2.0-M5")
    compile(group = "org.codehaus.groovy", name = "groovy", version = "2.3.7")
    compile(group = "org.apache.ivy", name = "ivy", version = "2.2.0")
    compile(group = "commons-lang", name = "commons-lang", version = "2.6")
    compile(group = "commons-codec", name = "commons-codec", version = "1.7")
    compile(group = "org.kohsuke.stapler", name = "stapler", version = "1.237")
    compile(group = "org.eclipse.jgit", name = "org.eclipse.jgit", version = "4.0.1.201506240215-r")
    compile(group = "com.squareup.okhttp", name = "okhttp-urlconnection", version = "2.0.0")
    compile(group = "org.kohsuke", name = "wordnet-random-name", version = "1.2")
    compile(group = "com.infradna.tool", name = "bridge-method-injector", version = "1.14")
    compile(group = "org.kohsuke", name = "github-api", version = "1.66")
    compile(group = "com.miglayout", name = "miglayout-swing", version = "4.2")
    compile(group = "commons-io", name = "commons-io", version = "2.4")
    compile(group = "org.python", name = "jython", version = "2.5.3")
    compile(group = "org.python", name = "jython-standalone", version = "2.5.2")
    compile(group = "org.clojure", name = "clojure", version = "1.8.0")
    compile(group = "org.eclipse.jetty", name = "jetty-server", version = "9.0.2.v20130417")
    compile(group = "org.eclipse.jetty", name = "jetty-servlet", version = "9.0.2.v20130417")
    compile(group = "org.eclipse.jetty", name = "jetty-servlets", version = "9.0.2.v20130417")
    compile(group = "org.eclipse.jetty", name = "jetty-webapp", version = "9.0.2.v20130417")
    compile(group = "javax.servlet", name = "javax.servlet-api", version = "3.1.0")
    compile(group = "java3d", name = "vecmath", version = "1.3.1")
    compile(group = "org.slf4j", name = "slf4j-simple", version = "1.6.1")
    compile(group = "com.neuronrobotics", name = "JavaCad", version = "0.13.5")
    compile(group = "com.neuronrobotics", name = "java-bowler", version = "3.25.0")
    compile(group = "jexcelapi", name = "jxl", version = "2.4.2")
    compile(group = "de.huxhorn.sulky", name = "de.huxhorn.sulky.3rdparty.jlayer", version = "1.0")
    compile(group = "com.google.code.gson", name = "gson", version = "2.5")
    compile(group = "org.jsoup", name = "jsoup", version = "1.8.3")
    compile(group = "org.apache.httpcomponents", name = "httpclient", version = "4.5.1")
    compile(group = "javax.media", name = "jmf", version = "2.1.1e")
    compile(group = "com.github.kurbatov", name = "firmata4j", version = "2.3.4.1")
    compile(fileTree("libs"))

    var baseDir: String

    if (isWindows()) {
        baseDir = System.getenv("OPENCV_DIR") + "\\..\\..\\java\\opencv-249.jar"
        println("OPENCV_DIR=$baseDir")
        compile(files(baseDir))
    }

    if (isOSX()) {
        baseDir = System.getenv("OPENCV_DIR") + "../../java/opencv-249.jar"
        println("OPENCV_DIR=$baseDir")

        if (System.getenv("OPENCV_DIR") != null) {
            compile(files(baseDir))
        } else {
            compile(files("/Applications/BowlerStudio.app/Contents/MacOS/opencv249build/bin/opencv-249.jar"))
        }
    }

    if (isLinux()) {
        if (File("/usr/share/OpenCV/java/").exists()) {
            println("Using the legacy opencv dir")
            compile(fileTree("/usr/share/OpenCV/java/") {
                include("*opencv-24*.jar")
            })
        } else {
            compile(fileTree("/usr/share/java/") {
                include("*opencv-24*.jar")
            })
        }
    }
}

spotless {
    java {
        licenseHeaderFile("${rootProject.rootDir}/config/spotless/bowler-script-kernel.license", "(package|import)")
    }
    kotlin {
        licenseHeaderFile("${rootProject.rootDir}/config/spotless/bowler-script-kernel.license", "(package|import)")
    }
}

checkstyle {
    configFile = file("${rootProject.rootDir}/config/checkstyle/checkstyle.xml")
}
