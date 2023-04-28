// run with
//      ./gradlew :examples:run --args="grid.mas2j  --no-net"

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jason-lang:jason-interpreter:3.2.0")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation(project(":"))
}

application {
    //mainClass.set("example.grid.MainKt")
    mainClass.set("jason.infra.local.RunLocalMAS")
}
