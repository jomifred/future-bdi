// run with
//      ./gradlew :examples:run --args="grid2.mas2j"
// or   ./gradlew :examples:run --args="bridge1.mas2j"

plugins {
    kotlin("jvm") version "2.2.0"
    application
}

repositories {
    mavenCentral()
    maven {url = uri("https://raw.githubusercontent.com/jacamo-lang/mvn-repo/master") }
}

dependencies {
    implementation("io.github.jason-lang:jason-interpreter:3.3.0")
    implementation("org.apache.commons:commons-csv:1.10.0")

    implementation(project(":"))
}

application {
    //mainClass.set("example.grid.MainKt")
    mainClass.set("jason.infra.local.RunLocalMAS")
}

task("genCSV", JavaExec::class) {
    mainClass.set("example.tools.GenerateTimeOutCSVKt")
    classpath = sourceSets["main"].runtimeClasspath
    //jvmArgs= listOf( "-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=y")
}
