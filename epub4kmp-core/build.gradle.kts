@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.maven.central.publish)
  alias(libs.plugins.dokka)
}

dokka {
  moduleName.set("epub4kmp-core")
  dokkaSourceSets.configureEach {
    includes.from("Module.md")
    sourceLink {
      localDirectory.set(rootDir)
      remoteUrl("https://github.com/Darkrock-Studios/epub4kmp/blob/main")
      remoteLineSuffix.set("#L")
    }
  }
}

kotlin {
  applyDefaultHierarchyTemplate()

  jvm()

  wasmJs {
    browser()
  }

  iosArm64()
  iosSimulatorArm64()

  macosArm64()

  linuxArm64()
  linuxX64()

  mingwX64()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.okio)
        implementation(libs.xmlutil.core)
        implementation(libs.kmp.zip)
        implementation(libs.kmp.zip.okio)
        implementation(libs.kotlinx.datetime)
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
    jvmTest {
      dependencies {
        implementation(libs.okio.fakefilesystem)
      }
    }
  }
}

mavenPublishing {
  publishToMavenCentral(automaticRelease = true)
  // signAllPublications() requires a configured GPG signing key — see
  // https://vanniktech.github.io/gradle-maven-publish-plugin/central/#secrets
  signAllPublications()

  coordinates(
    groupId = project.group.toString(),
    artifactId = "epub4kmp-core",
    version = project.version.toString(),
  )

  pom {
    name.set("epub4kmp-core")
    description.set(
      "Kotlin Multiplatform library for reading/writing/manipulating EPUB files. " +
        "A KMP fork of epub4j (formerly epub4j-kotlin, which itself was a fork of epublib)."
    )
    url.set("https://github.com/Darkrock-Studios/epub4kmp")
    licenses {
      license {
        name.set("Apache License, Version 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("repo")
      }
    }
    developers {
      developer {
        id.set("darkrockstudios")
        name.set("Adam Brown")
        email.set("adamwbrown@gmail.com")
      }
    }
    scm {
      connection.set("scm:git:git://github.com/Darkrock-Studios/epub4kmp.git")
      developerConnection.set("scm:git:ssh://github.com/Darkrock-Studios/epub4kmp.git")
      url.set("https://github.com/Darkrock-Studios/epub4kmp")
    }
  }
}
