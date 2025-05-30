import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cn.qiuzhanghua"
version = "0.1.0"

repositories {
  mavenCentral()
}

val vertxVersion = "5.0.0"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "cn.qiuzhanghua.vp.Proxy"
val launcherClassName = "io.vertx.launcher.application.VertxApplication"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation("io.github.cdimascio:dotenv-java:3.2.0")
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-launcher-application")
  implementation("io.vertx:vertx-http-proxy")
  implementation("io.vertx:vertx-web-proxy")
  implementation("io.vertx:vertx-opentelemetry")
  implementation("io.vertx:vertx-redis-client")
  implementation("ch.qos.logback:logback-classic:1.5.18")
  implementation("org.slf4j:slf4j-api:2.0.17")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf(mainVerticleName)
}
