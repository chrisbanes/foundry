/*
 * Copyright (C) 2022 Slack Technologies, LLC
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
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

pluginManagement {
  // Non-delegate APIs are annoyingly not public so we have to use withGroovyBuilder
  fun hasProperty(key: String): Boolean {
    return settings.withGroovyBuilder { "hasProperty"(key) as Boolean }
  }

  repositories {
    // Repos are declared roughly in order of likely to hit.

    // Snapshots/local go first in order to pre-empty other repos that may contain unscrupulous
    // snapshots.
    if (hasProperty("foundry.gradle.config.enableSnapshots")) {
      maven("https://oss.sonatype.org/content/repositories/snapshots")
      maven("https://androidx.dev/snapshots/latest/artifacts/repository")
      maven("https://oss.jfrog.org/libs-snapshot")
    }

    if (hasProperty("foundry.gradle.config.enableMavenLocal")) {
      mavenLocal()
    }

    mavenCentral()

    google()

    // Kotlin bootstrap repository, useful for testing against Kotlin dev builds. Usually only
    // tested on CI shadow jobs
    // https://kotlinlang.slack.com/archives/C0KLZSCHF/p1616514468003200?thread_ts=1616509748.001400&cid=C0KLZSCHF
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap") {
      name = "Kotlin-Bootstrap"
      content {
        // this repository *only* contains Kotlin artifacts (don't try others here)
        includeGroupByRegex("org\\.jetbrains.*")
      }
    }

    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

    gradlePluginPortal()
  }
}

buildscript {
  dependencies {
    // Force a newer version of okio, otherwise intellijPlatform and wire conflict
    classpath("com.squareup.okio:okio:3.15.0")
    // For some reason we need to enforce this on the classpath early
    // See https://github.com/tinyspeck/slack-android-ng/pull/107076
    classpath("com.squareup:kotlinpoet:2.2.0")
  }
}

plugins {
  id("com.gradle.develocity") version "4.1"
  id("org.jetbrains.intellij.platform.settings") version "2.7.0"
  id("org.jetbrains.intellij.platform") version "2.7.0" apply false
  // https://github.com/joshfriend/spotlight
  id("com.fueledbycaffeine.spotlight") version "1.3.0"
}

dependencyResolutionManagement {
  versionCatalogs {
    if (System.getenv("DEP_OVERRIDES") == "true") {
      val overrides = System.getenv().filterKeys { it.startsWith("DEP_OVERRIDE_") }
      maybeCreate("libs").apply {
        for ((key, value) in overrides) {
          val catalogKey = key.removePrefix("DEP_OVERRIDE_").lowercase()
          println("Overriding $catalogKey with $value")
          version(catalogKey, value)
        }
      }
    }
  }

  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

  // Non-delegate APIs are annoyingly not public so we have to use withGroovyBuilder
  fun hasProperty(key: String): Boolean {
    return settings.withGroovyBuilder { "hasProperty"(key) as Boolean }
  }

  repositories {
    // Repos are declared roughly in order of likely to hit.

    // Snapshots/local go first in order to pre-empty other repos that may contain unscrupulous
    // snapshots.
    if (hasProperty("foundry.gradle.config.enableSnapshots")) {
      maven("https://oss.sonatype.org/content/repositories/snapshots")
      maven("https://androidx.dev/snapshots/latest/artifacts/repository")
      maven("https://oss.jfrog.org/libs-snapshot")
    }

    if (hasProperty("foundry.gradle.config.enableMavenLocal")) {
      mavenLocal()
    }

    mavenCentral()

    google()

    // Kotlin bootstrap repository, useful for testing against Kotlin dev builds. Usually only
    // tested on CI shadow jobs
    // https://kotlinlang.slack.com/archives/C0KLZSCHF/p1616514468003200?thread_ts=1616509748.001400&cid=C0KLZSCHF
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap") {
      name = "Kotlin-Bootstrap"
      content {
        // this repository *only* contains Kotlin artifacts (don't try others here)
        includeGroupByRegex("org\\.jetbrains.*")
      }
    }

    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

    intellijPlatform { defaultRepositories() }

    gradlePluginPortal()
  }
}

val VERSION_NAME: String by extra.properties

develocity {
  buildScan {
    termsOfUseUrl.set("https://gradle.com/terms-of-service")
    termsOfUseAgree.set("yes")

    tag(if (System.getenv("CI").isNullOrBlank()) "Local" else "CI")
    tag(VERSION_NAME)
  }
}

rootProject.name = "foundry"

// https://docs.gradle.org/5.6/userguide/groovy_plugin.html#sec:groovy_compilation_avoidance
enableFeaturePreview("GROOVY_COMPILATION_AVOIDANCE")
