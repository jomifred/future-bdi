plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.jason-lang:jason-interpreter:3.2.0")
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