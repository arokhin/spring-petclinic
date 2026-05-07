import jetbrains.buildServer.configs.kotlin.*

class StandardPipeline(
    private val prefix: String
) {

    val build = standardBuildType(prefix, "Build") {
        steps {
            gradleScript("Clean", "clean")
        }
    }

    val qodana = standardBuildType(prefix, "Qodana") {
        steps {
            qodanaScript()
        }

        dependsOnBuild(build)
    }

    val testUnit = standardBuildType(prefix, "Test_Unit") {
        steps {
            gradleScript("Test", "test")
        }

        dependsOnBuild(build)
    }

    val testIntegration = standardBuildType(prefix, "Test_Integration") {
        steps {
            gradleScript("Integration Test", "integrationTest")
        }

        dependsOnBuild(build)
    }

    val allTests = standardBuildType(prefix, "AllTests") {
        type = BuildTypeSettings.Type.COMPOSITE

        dependsOnBuild(testUnit, testIntegration)
    }

    val deploy = standardBuildType(prefix, "Deploy") {
        name = buildName(prefix, "Deploy to preproduction")
        type = BuildTypeSettings.Type.DEPLOYMENT

        steps {
            gradleScript("Deploy to preproduction", "publish")
        }

        dependsOnBuild(allTests)
    }

    fun registerIn(project: Project) {
        project.buildType(build)
        project.buildType(qodana)
        project.buildType(testUnit)
        project.buildType(testIntegration)
        project.buildType(allTests)
        project.buildType(deploy)
    }
}

