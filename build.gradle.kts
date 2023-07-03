plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "io.github.jason-lang"
version = "1.3"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jason-lang:jason-interpreter:3.3.0-SNAPSHOT")
    //implementation( project(":examples"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "1.8"
//}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("example.grid.MainKt")
}