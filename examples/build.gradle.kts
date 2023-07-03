// run with
//      ./gradlew :examples:run --args="grid1.mas2j  --no-net"
// or   ./gradlew :examples:run --args="bridge1.mas2j  --no-net"

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jason-lang:jason-interpreter:3.3.0-SNAPSHOT")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation(project(":"))
}

application {
    //mainClass.set("example.grid.MainKt")
    mainClass.set("jason.infra.local.RunLocalMAS")
}
