package io.github.erabti.patois.plugin.plugin

import io.github.erabti.patois.plugin.PatoisPlugin
import io.github.erabti.patois.plugin.models.PatoisPluginExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class PackageNameTest : FunSpec({
    fun projectIn(dir: File): Project {
        val project = ProjectBuilder.builder().withProjectDir(dir).build()
        project.pluginManager.apply("java")
        project.tasks.register("compileKotlin")
        return project
    }

    fun Project.patois(): PatoisPluginExtension = extensions.getByType(PatoisPluginExtension::class.java)

    test("a project that names its package never has its sources read") {
        val dir = tempdir()
        dir.resolve("src/main/kotlin/Unreadable.kt").mkdirs()
        val project = projectIn(dir)

        project.pluginManager.apply(PatoisPlugin::class.java)
        project.patois().packageName.set("com.example.strings")

        project.patois().packageName.get() shouldBe "com.example.strings"
    }

    test("a project that names no package gets the package of its sources") {
        val dir = tempdir()
        val source = dir.resolve("src/main/kotlin/com/example/Main.kt")
        source.parentFile.mkdirs()
        source.writeText("package com.example\n\nfun main() = Unit\n")
        val project = projectIn(dir)

        project.pluginManager.apply(PatoisPlugin::class.java)

        project.patois().packageName.get() shouldBe "com.example"
    }

    test("a project with no sources and no package name gets its group") {
        val project = projectIn(tempdir())
        project.group = "com.example.group"

        project.pluginManager.apply(PatoisPlugin::class.java)

        project.patois().packageName.get() shouldBe "com.example.group"
    }
})
