import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

version = "2025.11"

object HttpsGithubComArokhinSpringPetclinicGit : GitVcsRoot({
    name = "https://github.com/arokhin/spring-petclinic.git"
    url = "https://github.com/arokhin/spring-petclinic.git"
    branch = "heavy-ai-project"
})

project {
    description = "Reference CI project for repositories with substantial AI-generated or AI-assisted code contribution."

    params {
        param("repo.url", "https://github.com/your-org/your-repo.git")
        param("repo.branch", "refs/heads/main")
        password("github.token", "credentialsJSON:CHANGE_ME")
        param("artifact.name", "ai-heavy-service")
        param("policy.mode", "strict")
    }

    vcsRoot(HttpsGithubComArokhinSpringPetclinicGit)

    template(SharedCiFoundation)

    buildType(ValidateAll)
    buildType(UnitTests)
    buildType(IntegrationTests)
    buildType(RegressionTests)
    buildType(EndToEndTests)
    buildType(StaticAnalysis)
    buildType(SecurityScanning)
    buildType(PolicyAsCode)
    buildType(PackageArtifact)
    buildType(DeployStaging)
}


object SharedCiFoundation : Template({
    id("SharedCiFoundation")
    name = "Shared CI foundation"
    description = "Common validation, traceability, and reporting behavior reused by all build configurations."

    vcs {
        root(HttpsGithubComArokhinSpringPetclinicGit)
    }

    artifactRules = """
        build/reports/** => reports.zip
        reports/** => reports.zip
        sbom/** => sbom.zip
        provenance/** => provenance.zip
    """.trimIndent()

    features {
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "%github.token%"
                }
            }
        }
    }

    failureConditions {
        executionTimeoutMin = 45
        nonZeroExitCode = true
    }

    requirements {
        exists("teamcity.agent.jvm.os.name")
    }
})

object ValidateAll : BuildType({
    id("ValidateAll")
    name = "00 Validate all gates"
    description = "Single entry point: every change follows the same validation chain."

    templates(SharedCiFoundation)

    triggers {
        vcs {
            branchFilter = "+:*"
            triggerRules = "-:.teamcity/**"
        }
    }

    dependencies {
        snapshot(UnitTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(IntegrationTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(RegressionTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(EndToEndTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(StaticAnalysis) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(SecurityScanning) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(PolicyAsCode) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(PackageArtifact) { onDependencyFailure = FailureAction.FAIL_TO_START }
    }

    steps {
        script {
            name = "Summarize validation result"
            scriptContent = """
                echo "All mandatory validation gates passed for %teamcity.build.branch% at %build.vcs.number%"
            """.trimIndent()
        }
    }
})

object UnitTests : BuildType({
    id("UnitTests")
    name = "01 Unit tests"
    templates(SharedCiFoundation)

    steps {
        gradle {
            name = "Run unit tests"
            tasks = "clean test"
            gradleParams = "--no-daemon"
            useGradleWrapper = true
            gradleWrapperPath = "."
        }
    }
})

object IntegrationTests : BuildType({
    id("IntegrationTests")
    name = "02 Integration tests"
    templates(SharedCiFoundation)

    dependencies {
        snapshot(UnitTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
    }

    steps {
        script {
            name = "Run integration tests"
            scriptContent = """
                ./gradlew --no-daemon integrationTest
            """.trimIndent()
        }
    }
})

object RegressionTests : BuildType({
    id("RegressionTests")
    name = "03 Regression tests"
    templates(SharedCiFoundation)

    dependencies {
        snapshot(UnitTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
    }

    steps {
        script {
            name = "Run regression suite"
            scriptContent = """
                ./gradlew --no-daemon regressionTest
            """.trimIndent()
        }
    }
})

object EndToEndTests : BuildType({
    id("EndToEndTests")
    name = "04 End-to-end tests"
    templates(SharedCiFoundation)

    dependencies {
        snapshot(IntegrationTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
    }

    steps {
        script {
            name = "Run E2E tests"
            scriptContent = """
                ./gradlew --no-daemon e2eTest
            """.trimIndent()
        }
    }
})

object StaticAnalysis : BuildType({
    id("StaticAnalysis")
    name = "05 Static analysis and quality gates"
    templates(SharedCiFoundation)

    steps {
        script {
            name = "Style, duplication, complexity, maintainability"
            scriptContent = """
                ./gradlew --no-daemon ktlintCheck detekt checkstyleMain
                ./gradlew --no-daemon jacocoTestReport

                # Example maintainability gate. Replace with your real thresholds.
                test -f build/reports/detekt/detekt.xml
            """.trimIndent()
        }
    }

//    failureConditions {
//        nonZeroExitCode = true
//        failOnMetricChange {
//            metric = BuildFailureOnMetric.MetricType.INSPECTION_WARN_COUNT
//            threshold = 1
//            units = BuildFailureOnMetric.MetricUnit.DEFAULT_UNIT
//            comparison = BuildFailureOnMetric.MetricComparison.MORE
//            compareTo = build {
//                buildRule = lastSuccessful()
//            }
//        }
//    }
})

object SecurityScanning : BuildType({
    id("SecurityScanning")
    name = "06 Security scanning"
    templates(SharedCiFoundation)

    steps {
        script {
            name = "Dependencies, secrets, vulnerabilities, infrastructure"
            scriptContent = """
                mkdir -p reports/security sbom

                # Dependency and vulnerability scanning examples.
                ./gradlew --no-daemon dependencyCheckAnalyze

                # Secret scanning example. Replace with your approved scanner.
                gitleaks detect --source . --report-format sarif --report-path reports/security/gitleaks.sarif

                # Infrastructure config scanning example. Replace with Checkov, Trivy, KICS, or your standard tool.
                if [ -d infra ]; then
                  checkov -d infra -o sarif --output-file-path reports/security/checkov.sarif
                fi

                # SBOM generation example. Replace with your standard SBOM tool if needed.
                syft dir:. -o spdx-json=sbom/sbom.spdx.json
            """.trimIndent()
        }
    }
})

object PolicyAsCode : BuildType({
    id("PolicyAsCode")
    name = "07 Policy-as-code"
    templates(SharedCiFoundation)

    steps {
        script {
            name = "Enforce executable engineering rules"
            scriptContent = """
                mkdir -p reports/policy

                # Example using Open Policy Agent.
                # Policies can cover approvals, dependency allowlists, deployment rules, IaC constraints, etc.
                conftest test \
                  --policy policy \
                  --output junit \
                  . > reports/policy/conftest.xml

                # Example: require strict mode for main branch.
                if [ "%teamcity.build.branch%" = "main" ] && [ "%policy.mode%" != "strict" ]; then
                  echo "Main branch requires strict policy mode"
                  exit 1
                fi
            """.trimIndent()
        }
    }
})

object PackageArtifact : BuildType({
    id("PackageArtifact")
    name = "08 Package artifact with provenance"
    templates(SharedCiFoundation)

    dependencies {
        snapshot(UnitTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(StaticAnalysis) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(SecurityScanning) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(PolicyAsCode) { onDependencyFailure = FailureAction.FAIL_TO_START }
    }

    artifactRules = """
        build/libs/*.jar => packages
        build/distributions/** => packages
        sbom/** => sbom.zip
        provenance/** => provenance.zip
    """.trimIndent()

    steps {
        script {
            name = "Build package"
            scriptContent = """
                ./gradlew --no-daemon clean assemble
            """.trimIndent()
        }
        script {
            name = "Generate traceability metadata"
            scriptContent = """
                mkdir -p provenance
                cat > provenance/build-provenance.json <<EOF
                {
                  "artifact": "%artifact.name%",
                  "buildId": "%teamcity.build.id%",
                  "buildNumber": "%build.number%",
                  "branch": "%teamcity.build.branch%",
                  "revision": "%build.vcs.number%",
                  "project": "%system.teamcity.projectName%",
                  "configuration": "%system.teamcity.buildConfName%",
                  "serverUrl": "%teamcity.serverUrl%"
                }
                EOF
            """.trimIndent()
        }
    }
})

object DeployStaging : BuildType({
    id("DeployStaging")
    name = "09 Deploy to staging"
    description = "Deployment is allowed only after all validation, security, policy, and packaging gates pass."
    templates(SharedCiFoundation)

    dependencies {
        snapshot(ValidateAll) { onDependencyFailure = FailureAction.FAIL_TO_START }
        artifacts(PackageArtifact) {
            artifactRules = "packages/** => input/packages"
        }
    }

    steps {
        script {
            name = "Deploy staging"
            scriptContent = """
                echo "Deploying %artifact.name% from revision %build.vcs.number% to staging"
                ./scripts/deploy-staging.sh input/packages
            """.trimIndent()
        }
    }
})
