package tests

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import templates.*


object ServiceTests : BuildType({
    id("ServiceTests")
    name = "Service: Tests"
    description = "Runs the service test suite using the shared TestsTemplate baseline."

    templates(UnitTestsTemplate)

    params {
        // Keep this enabled so tests are mandatory for the build.
        param("teamcity.tests.run", "true")
    }

    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }

    steps {

    }

    triggers {
        vcs {
            branchFilter = """
                +:<default>
                +:refs/heads/feature/*
                +:refs/heads/bugfix/*
            """.trimIndent()
        }
    }

    failureConditions {
        // Override the template timeout if this test suite needs more time.
        executionTimeoutMin = 45
    }
})
