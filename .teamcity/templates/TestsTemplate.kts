package templates

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.XmlReport
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs


/*
 * TeamCity Kotlin DSL template for mandatory test execution.
 *
 * Usage:
 *   1. Place this object in your .teamcity/settings.kts or import it from a separate Kotlin file.
 *   2. Register it in your project with: template(TestsTemplate)
 *   3. Attach it to build configurations with: templates(TestsTemplate)
 *
 * The template is intentionally generic: concrete projects override parameters
 * instead of redefining the test logic.
 */
object TestsTemplate : Template({
    id("TestsTemplate")
    name = "Template: Tests"
    description = "Reusable baseline for running automated tests and publishing JUnit reports."

    params {
        // Override these values in build configurations based on this template.
        param("test.command", "./gradlew test")
        param("test.reports", "build/test-results/test/**/*.xml")
        param("test.artifacts", "build/reports/tests => test-reports.zip")
        param("teamcity.tests.run", "true")
    }

    steps {
        script {
            name = "Run tests"
            executionMode = BuildStep.ExecutionMode.RUN_ON_SUCCESS
            scriptContent = """
                set -euo pipefail

                if [ "%teamcity.tests.run%" != "true" ]; then
                  echo "Tests are disabled for this build configuration."
                  exit 1
                fi

                echo "Running test command: %test.command%"
                %test.command%
            """.trimIndent()
        }
    }

    features {
        xmlReport {
            id = "JUnitReports"
            reportType = XmlReport.XmlReportType.JUNIT
            rules = "+:%test.reports%"
            verbose = true
        }
    }

    artifactRules = "%test.artifacts%"

    failureConditions {
        executionTimeoutMin = 30
    }
})

/*
 * Example build configuration using the template.
 * Register both objects in your project:
 *
 * project {
 *     template(TestsTemplate)
 *     buildType(ExampleServiceTests)
 * }
 */
object ExampleServiceTests : BuildType({
    id("ExampleServiceTests")
    name = "Example Service: Tests"

    templates(TestsTemplate)

    params {
        param("test.command", "./gradlew clean test")
        param("test.reports", "build/test-results/test/**/*.xml")
        param("test.artifacts", "build/reports/tests => test-reports.zip")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
            branchFilter = "+:<default>"
        }
    }
})
