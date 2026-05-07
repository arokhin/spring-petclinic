import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script

/**
 * Configures the VCS settings of the build type to use the root directory of the project where the settings are defined.
 *
 * This function sets the VCS root of the build configuration by referencing the `settingsRoot` context,
 * which refers to the root of the project containing the build configuration script. By doing so, the build configuration
 * will operate on the code from the project's root directory by default.
 *
 * Typically used in build pipelines to ensure all build configurations in a project operate on the same VCS root.
 */
fun BuildType.useSettingsRoot() {
    vcs {
        root(DslContext.settingsRoot)
    }
}

/**
 * Adds snapshot dependencies to the current build configuration for one or more specified build configurations.
 *
 * @param buildTypes One or more build configurations to which the current build configuration should depend on.
 * Each provided build configuration will be added as a snapshot dependency.
 */
fun BuildType.dependsOnBuild(vararg buildTypes: BuildType) {
    dependencies {
        buildTypes.forEach { buildType ->
            snapshot(buildType) {}
        }
    }
}

/**
 * Adds a Gradle script step to the build pipeline.
 *
 * @param name The name of the script step to be displayed in the build configuration.
 * @param task The Gradle task to execute, such as "clean", "test", "build", etc.
 */
fun BuildSteps.gradleScript(name: String, task: String) {
    script {
        this.name = name
        scriptContent = "./gradlew $task"
    }
}

/**
 * Adds a Qodana code quality check step using Docker.
 *
 * This step runs Qodana JVM analysis in a Docker container with the project mounted
 * and shows the report after completion.
 *
 * @param name The name of the Qodana step (defaults to "Qodana").
 */
fun BuildSteps.qodanaScript(name: String = "Qodana") {
    script {
        this.name = name
        scriptContent = """
            docker run --rm \
              -v %system.teamcity.build.checkoutDir%:/data/project/ \
              -v qodana-cache:/data/cache/ \
              jetbrains/qodana-jvm:latest \
              --show-report
        """.trimIndent()
    }
}

/**
 * Generates a build configuration ID from a prefix and suffix.
 *
 * @param prefix The prefix identifying the pipeline or project.
 * @param suffix The suffix identifying the specific build type.
 * @return A formatted ID string with underscore separators.
 */
fun buildId(prefix: String, suffix: String): String = "${prefix}_${suffix}"

/**
 * Generates a build configuration display name from a prefix and suffix.
 *
 * @param prefix The prefix identifying the pipeline or project.
 * @param suffix The suffix identifying the specific build type.
 * @return A formatted name string with :: separators.
 */
fun buildName(prefix: String, suffix: String): String = "$prefix :: $suffix"

/**
 * Creates a standard build type with common configuration applied.
 *
 * This function provides a DSL-style builder for creating build types with:
 * - Automatic ID generation based on prefix and suffix
 * - Automatic name generation with consistent formatting
 * - VCS root configuration using settingsRoot
 * - Optional custom configuration via the block parameter
 *
 * @param prefix The prefix for the build type (typically the pipeline name).
 * @param suffix The suffix for the build type (e.g., "Build", "Test_Unit").
 * @param block Additional configuration to apply to the build type.
 * @return A configured BuildType instance.
 */
fun standardBuildType(prefix: String, suffix: String, block: BuildType.() -> Unit = {}): BuildType {
    return BuildType {
        id(buildId(prefix, suffix))
        name = buildName(prefix, suffix)
        useSettingsRoot()
        block()
    }
}
