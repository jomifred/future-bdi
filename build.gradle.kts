plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "io.github.jason-lang"
version = "1.6"

repositories {
    mavenCentral()
    maven {url = uri("https://raw.githubusercontent.com/jacamo-lang/mvn-repo/master") }
}

dependencies {
    implementation("io.github.jason-lang:jason-interpreter:3.3.0-SNAPSHOT")
    implementation("org.jacamo:npl:0.6-SNAPSHOT")
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