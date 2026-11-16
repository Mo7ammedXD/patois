package io.github.erabti.patois.plugin.runners

import io.github.erabti.patois.models.PatoisConfig
import io.github.erabti.patois.plugin.application.runners.GenerateTranslationRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class ReusedStringsTest : FunSpec({
    fun generate(): String {
        val input = tempdir()
        val output = tempdir()
        input.resolve("en.yaml").writeText("greeting: Hello")
        input.resolve("ar.yaml").writeText("greeting: مرحبا")

        GenerateTranslationRunner(
            config = PatoisConfig(
                className = "AppStrings",
                packageName = "com.example",
                inputDir = input.absolutePath,
                outputDir = output.absolutePath,
                baseLocale = "en",
                enumName = "AppLocale",
                resolverName = "AppLocaleResolver",
            ),
            logger = {},
        ).run()

        return output.resolve("com/example/AppStrings.kt").readText()
    }

    test("a locale builds its strings once and gives the same object on every call") {
        val generated = generate()

        generated shouldContain "private val strings: AppStrings by lazy {"
        generated shouldContain "override fun toStrings(): AppStrings = strings"
    }

    test("toStrings no longer builds a new object for each call") {
        val toStrings = generate().substringAfter("fun toStrings()").substringBefore("override val config")

        toStrings shouldNotContain "AppStringsEn()"
        toStrings shouldNotContain "AppStringsAr()"
    }
})
