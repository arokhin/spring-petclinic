package templates

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.XmlReport
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object UnitTestsTemplate : Template({
    id("UnitTestsTemplate")
    name = "Template: Unit Tests"
    description = "Reusable template for running unit tests and publishing JUnit reports."

    params {
        param("test.command", "./gradlew test")
        param("test.reports", "build/test-results/test/**/*.xml")
        param("test.artifacts", "build/reports/tests => test-reports.zip")
    }

    steps {
        script {
            name = "Run unit tests"
            executionMode = BuildStep.ExecutionMode.RUN_ON_SUCCESS
            scriptContent = """
                set -euo pipefail
                
                echo "Running unit tests: %test.command%"
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
