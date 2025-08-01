/*
 * Copyright (C) 2023 Slack Technologies, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.bestPracticesPlugin)
  alias(libs.plugins.moshix)
  alias(libs.plugins.buildConfig)
  alias(libs.plugins.lint)
}

gradlePlugin {
  plugins.create("foundry-root") {
    id = "com.slack.foundry.root"
    implementationClass = "foundry.gradle.FoundryRootPlugin"
  }
  plugins.create("foundry-base") {
    id = "com.slack.foundry.base"
    implementationClass = "foundry.gradle.FoundryBasePlugin"
  }
  plugins.create("apkVersioning") {
    id = "com.slack.foundry.apk-versioning"
    implementationClass = "foundry.gradle.ApkVersioningPlugin"
  }
}

buildConfig {
  packageName("foundry.gradle.dependencies")
  useKotlinOutput { internalVisibility = true }
}

// Copy our hooks into resources for InstallCommitHooks
tasks.named<ProcessResources>("processResources") {
  from(rootProject.layout.projectDirectory.dir("config/git/hooks")) {
    // Give it a common prefix for us to look for
    rename { name -> "githook-$name" }
  }
}

moshi { enableSealed.set(true) }

tasks.named<ValidatePlugins>("validatePlugins") { enableStricterValidation.set(true) }

// This is necessary for included builds, as the KGP plugin isn't applied in them and thus doesn't
// apply disambiguation rules
dependencies.constraints {
  add("implementation", "io.github.pdvrieze.xmlutil:serialization") {
    attributes { attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm) }
  }
  add("implementation", "io.github.pdvrieze.xmlutil:core") {
    attributes { attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm) }
  }
}

dependencies {
  api(platform(libs.okhttp.bom))
  api(project(":platforms:gradle:agp-handlers:agp-handler-api"))
  api(project(":tools:version-number"))
  api(libs.okhttp)
  // Better I/O
  api(libs.okio)

  implementation(platform(libs.coroutines.bom))
  implementation(project(":platforms:gradle:better-gradle-properties"))
  implementation(project(":tools:cli"))
  implementation(project(":tools:foundry-common"))
  implementation(project(":tools:robolectric-sdk-management"))
  implementation(project(":tools:skippy"))
  implementation(libs.commonsText) { because("For access to its StringEscapeUtils") }
  implementation(libs.coroutines.core)
  implementation(libs.develocity.agent.adapters)
  implementation(libs.gradlePlugins.graphAssert) { because("To use in Gradle graphing APIs.") }
  implementation(libs.guava)
  // Graphing library with Betweenness Centrality algo for modularization score
  implementation(libs.jgrapht)
  implementation(libs.jna)
  implementation(libs.jna.platform)
  implementation(libs.moshi)
  implementation(libs.oshi) { because("To read hardware information") }
  implementation(libs.rxjava)

  compileOnly(platform(libs.kotlin.bom))
  compileOnly(gradleApi())
  compileOnly(libs.agp)
  compileOnly(libs.detekt)
  compileOnly(libs.gradlePlugins.anvil)
  // compileOnly because we want to leave versioning to the consumers
  // Add gradle plugins for the slack project itself, separate from plugins. We do this so we can
  // de-dupe version management between this plugin and the root build.gradle.kts file.
  compileOnly(libs.gradlePlugins.bugsnag)
  compileOnly(libs.gradlePlugins.compose)
  compileOnly(libs.gradlePlugins.composeCompiler)
  compileOnly(libs.gradlePlugins.dependencyAnalysis)
  compileOnly(libs.gradlePlugins.detekt)
  compileOnly(libs.gradlePlugins.develocity)
  compileOnly(libs.gradlePlugins.doctor)
  compileOnly(libs.gradlePlugins.emulatorWtf)
  compileOnly(libs.gradlePlugins.errorProne)
  compileOnly(libs.gradlePlugins.kgp)
  compileOnly(libs.gradlePlugins.ksp)
  compileOnly(libs.gradlePlugins.metro)
  compileOnly(libs.gradlePlugins.moshix)
  compileOnly(libs.gradlePlugins.nullaway)
  compileOnly(libs.gradlePlugins.redacted)
  compileOnly(libs.gradlePlugins.retry)
  compileOnly(libs.gradlePlugins.spotless)
  compileOnly(libs.gradlePlugins.sqldelight)
  compileOnly(libs.gradlePlugins.wire)
  compileOnly(libs.kotlin.reflect)

  testImplementation(platform(libs.coroutines.bom))
  testImplementation(libs.agp)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.okio.fakefilesystem)
  testImplementation(libs.truth)

  lintChecks(libs.gradleLints)
}
