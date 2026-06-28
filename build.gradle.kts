plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.maven.central.publish) apply false
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.dokka)
}

allprojects {
  group = "com.darkrockstudios"
  version = providers.gradleProperty("library.version").get()
}

// Aggregates the two published library modules into a single API doc site.
dependencies {
  dokka(project(":epub4kmp-core"))
  dokka(project(":epub4kmp-compose-ui"))
}

dokka {
  moduleName.set("epub4kmp")
  dokkaPublications.html {
    // Lands in a subfolder of the GitHub Pages site so it coexists with the
    // wasm reader demo at the site root: darkrock-studios.github.io/epub4kmp/api/
    outputDirectory.set(rootDir.resolve("docs/api"))
  }
}

// Generates the API docs into docs/api for GitHub Pages. The wasm demo is built
// separately by :samples:reader-web:updateDemo (which only exists on the demo branch).
tasks.register("updateDocs") {
  description = "Generates the Dokka API docs into docs/api for GitHub Pages."
  group = "documentation"
  dependsOn("dokkaGenerate")
}
