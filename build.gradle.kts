plugins {
  java
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
  id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "com.fatsan1975"
version = "1.0.0"
description = "Fatsan Ore Generation"

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

dependencies {
  paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

  testImplementation(platform("org.junit:junit-bom:5.10.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release = 21
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }

  processResources {
    filteringCharset = Charsets.UTF_8.name()
    filesMatching("plugin.yml") {
      expand("version" to project.version)
    }
  }

  test {
    useJUnitPlatform()
  }
}
