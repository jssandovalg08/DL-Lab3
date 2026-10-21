plugins {
    id("java")
    id("application")
}

group = "me.jssandoval.lab3"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "me.jssandoval.lab3.Main"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}


tasks.test {
    useJUnitPlatform()
}