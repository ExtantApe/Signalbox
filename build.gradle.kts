plugins {
    `java-library`
    `maven-publish`
}

group = "com.extantape"
version = "0.3.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
}

// No dependencies at runtime: plugins shade and relocate Signalbox, so it stays plain Java.
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.test { useJUnitPlatform() }

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("signalbox") {
            from(components["java"])
            artifactId = "signalbox"
        }
    }
}
