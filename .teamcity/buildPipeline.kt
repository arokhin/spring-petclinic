import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script

class StandardPipeline(
    private val prefix: String
) {

    val build = BuildType {
        id("${prefix}_Build")
        name = "$prefix :: Build"

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            script {
                name = "Build"
                scriptContent = "./gradlew clean build"
            }
        }
    }

    val qodana = BuildType {
        id("${prefix}_Qodana")
        name = "$prefix :: Qodana"

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            script {
                name = "Qodana"
                scriptContent = """
                    docker run --rm \
                      -v %system.teamcity.build.checkoutDir%:/data/project/ \
                      -v qodana-cache:/data/cache/ \
                      jetbrains/qodana-jvm:latest \
                      --show-report
                """.trimIndent()
            }
        }

        dependencies {
            snapshot(build) {}
        }
    }

    val testUnit = BuildType {
        id("${prefix}_Test_Unit")
        name = "$prefix :: Test :: Unit"

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            script {
                name = "Unit tests"
                scriptContent = "./gradlew test"
            }
        }

        dependencies {
            snapshot(build) {}
        }
    }

    val testIntegration = BuildType {
        id("${prefix}_Test_Integration")
        name = "$prefix :: Test :: Integration"

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            script {
                name = "Integration tests"
                scriptContent = "./gradlew integrationTest"
            }
        }

        dependencies {
            snapshot(build) {}
        }
    }

    val testUi = BuildType {
        id("${prefix}_Test_UI")
        name = "$prefix :: Test :: UI"

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            script {
                name = "UI tests"
                scriptContent = "./gradlew uiTest"
            }
        }

        dependencies {
            snapshot(build) {}
        }
    }

    val allTests = BuildType {
        id("${prefix}_AllTests")
        name = "$prefix :: Tests (All)"
        type = BuildTypeSettings.Type.COMPOSITE

        vcs {
            root(DslContext.settingsRoot)
        }

        dependencies {
            snapshot(testUnit) {}
            snapshot(testIntegration) {}
            snapshot(testUi) {}
        }
    }

    val deploy = BuildType {
        id("${prefix}_Deploy")
        name = "$prefix :: Deploy to preproduction"
        type = BuildTypeSettings.Type.DEPLOYMENT

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            script {
                name = "Deploy"
                scriptContent = "./gradlew publish"
            }
        }

        dependencies {
            snapshot(allTests) {}
        }
    }

    fun registerIn(project: Project) {
        project.buildType(build)
        project.buildType(qodana)
        project.buildType(testUnit)
        project.buildType(testIntegration)
        project.buildType(testUi)
        project.buildType(allTests)
        project.buildType(deploy)
    }
}

