// run with
    //      ./gradlew :examples:run --args="grid.mas2j"

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jason-lang:jason-interpreter:3.2.0")
    implementation(project(":"))
}

application {
    //mainClass.set("example.grid.MainKt")
    mainClass.set("jason.infra.local.RunLocalMAS")
}
