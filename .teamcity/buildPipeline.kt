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

    val deploy = BuildType {
        id("${prefix}_Deploy_Preproduction")
        name = "$prefix :: Deploy"
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
            snapshot(testUnit) {}
            snapshot(testIntegration) {}
            snapshot(testUi) {}
        }
    }

    fun registerIn(project: Project) {
        project.buildType(build)
        project.buildType(testUnit)
        project.buildType(testIntegration)
        project.buildType(testUi)
        project.buildType(deploy)
    }
}
