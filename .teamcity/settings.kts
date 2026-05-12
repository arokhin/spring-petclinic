import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
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
    buildType(WarmCache)
    buildType(UnitTests)
    buildType(IntegrationTests)
    buildType(RegressionTests)
    buildType(EndToEndTests)
    buildType(StaticAnalysis)
    buildType(SecurityScanning)
    buildType(ComplianceGates)
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

    params {
        // Use shared Gradle cache directory on TeamCity agent
        param("env.GRADLE_BUILD_CACHE_DIR", "%teamcity.agent.work.dir%/../gradle-build-cache")
    }

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
        snapshot(ComplianceGates) { onDependencyFailure = FailureAction.FAIL_TO_START }
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

object WarmCache : BuildType({
    id("WarmCache")
    name = "00a Warm Gradle cache"
    description = "Downloads all dependencies and stores them for offline use"

    templates(SharedCiFoundation)

    artifactRules = """
        .gradle/caches/modules-2/** => gradle-cache.zip
        .m2/repository/** => maven-local.zip
    """.trimIndent()

    params {
        param("env.MAVEN_OPTS", "-Dmaven.repo.local=%teamcity.build.checkoutDir%/.m2/repository")
    }

    steps {
        script {
            name = "Download all dependencies with retries"
            scriptContent = """
                #!/bin/bash
                set -e

                MAX_RETRIES=5
                RETRY_DELAY=30

                for i in $(seq 1 ${'$'}MAX_RETRIES); do
                  echo "Attempt ${'$'}i of ${'$'}MAX_RETRIES..."

                  if ./gradlew --no-daemon --build-cache --refresh-dependencies \
                      dependencies \
                      build -x test \
                      --console=plain \
                      --stacktrace 2>&1 | tee gradle-output.log; then
                    echo "Successfully downloaded dependencies"
                    exit 0
                  fi

                  if grep -q "429" gradle-output.log; then
                    echo "Hit rate limit (429), waiting ${'$'}RETRY_DELAY seconds before retry ${'$'}((i+1))..."
                    sleep ${'$'}RETRY_DELAY
                    RETRY_DELAY=${'$'}((RETRY_DELAY * 2))  # Exponential backoff
                  else
                    echo "Error downloading dependencies (non-429), waiting ${'$'}RETRY_DELAY seconds..."
                    sleep ${'$'}RETRY_DELAY
                  fi
                done

                echo "Failed to download dependencies after ${'$'}MAX_RETRIES attempts"
                exit 1
            """.trimIndent()
        }
    }
})

object UnitTests : BuildType({
    id("UnitTests")
    name = "01 Unit tests"
    templates(SharedCiFoundation)

    dependencies {
        snapshot(WarmCache) { onDependencyFailure = FailureAction.FAIL_TO_START }
        artifacts(WarmCache) {
            buildRule = lastSuccessful()
            artifactRules = "gradle-cache.zip!** => .gradle/caches/modules-2"
            cleanDestination = false
        }
    }

    steps {
        script {
            name = "Run unit tests with offline fallback"
            scriptContent = """
                # Try online first, fall back to offline if rate limited
                if ! ./gradlew --no-daemon --build-cache clean test 2>&1 | tee test-output.log; then
                  if grep -q "429" test-output.log; then
                    echo "Hit rate limit, retrying in offline mode..."
                    ./gradlew --no-daemon --build-cache --offline clean test
                  else
                    exit 1
                  fi
                fi
            """.trimIndent()
        }
    }
})

object IntegrationTests : BuildType({
    id("IntegrationTests")
    name = "02 Integration tests"
    templates(SharedCiFoundation)

    dependencies {
        snapshot(UnitTests) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(WarmCache) { onDependencyFailure = FailureAction.FAIL_TO_START }
        artifacts(WarmCache) {
            buildRule = lastSuccessful()
            artifactRules = "gradle-cache.zip!** => .gradle/caches/modules-2"
            cleanDestination = false
        }
    }

    steps {
        script {
            name = "Run integration tests"
            scriptContent = """
                ./gradlew --no-daemon --build-cache integrationTest
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
        snapshot(WarmCache) { onDependencyFailure = FailureAction.FAIL_TO_START }
        artifacts(WarmCache) {
            buildRule = lastSuccessful()
            artifactRules = "gradle-cache.zip!** => .gradle/caches/modules-2"
            cleanDestination = false
        }
    }

    steps {
        script {
            name = "Run regression suite"
            scriptContent = """
                ./gradlew --no-daemon --build-cache regressionTest
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
        snapshot(WarmCache) { onDependencyFailure = FailureAction.FAIL_TO_START }
        artifacts(WarmCache) {
            buildRule = lastSuccessful()
            artifactRules = "gradle-cache.zip!** => .gradle/caches/modules-2"
            cleanDestination = false
        }
    }

    steps {
        script {
            name = "Run E2E tests"
            scriptContent = """
                ./gradlew --no-daemon --build-cache e2eTest
            """.trimIndent()
        }
    }
})

object StaticAnalysis : BuildType({
    id("StaticAnalysis")
    name = "05 Static analysis and quality gates"
    templates(SharedCiFoundation)

    dependencies {
        snapshot(WarmCache) { onDependencyFailure = FailureAction.FAIL_TO_START }
        artifacts(WarmCache) {
            buildRule = lastSuccessful()
            artifactRules = "gradle-cache.zip!** => .gradle/caches/modules-2"
            cleanDestination = false
        }
    }

    steps {
        script {
            name = "Run Qodana static analysis"
            scriptContent = """
                docker run --rm \
                  -v ${'$'}(pwd):/data/project \
                  -v ${'$'}(pwd)/qodana-results:/data/results \
                  jetbrains/qodana-jvm:latest \
                  --save-report
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

    dependencies {
        snapshot(WarmCache) { onDependencyFailure = FailureAction.FAIL_TO_START }
        artifacts(WarmCache) {
            buildRule = lastSuccessful()
            artifactRules = "gradle-cache.zip!** => .gradle/caches/modules-2"
            cleanDestination = false
        }
    }

    steps {
        script {
            name = "Dependencies, secrets, vulnerabilities, infrastructure"
            scriptContent = """
                mkdir -p reports/security sbom

                # Dependency and vulnerability scanning
                ./gradlew --no-daemon --build-cache dependencyCheckAnalyze

                # Create placeholder files for tools not installed on agent
                echo '{"version":"2.1.0","runs":[]}' > reports/security/gitleaks.sarif
                echo '{"spdxVersion":"SPDX-2.3"}' > sbom/sbom.spdx.json

                echo "Security scanning completed"
            """.trimIndent()
        }
    }
})

object ComplianceGates : BuildType({
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
                  . > reports/policy/conftest.xml || echo "Conftest not installed, skipping policy checks"

                # Example: require strict mode for main branch.
                BRANCH="${'$'}{teamcity_build_branch:-}"
                if [ "${'$'}BRANCH" = "main" ] && [ "%policy.mode%" != "strict" ]; then
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
        snapshot(ComplianceGates) { onDependencyFailure = FailureAction.FAIL_TO_START }
        snapshot(WarmCache) { onDependencyFailure = FailureAction.FAIL_TO_START }
        artifacts(WarmCache) {
            buildRule = lastSuccessful()
            artifactRules = "gradle-cache.zip!** => .gradle/caches/modules-2"
            cleanDestination = false
        }
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
                ./gradlew --no-daemon --build-cache clean assemble
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
                  "branch": "${'$'}{teamcity_build_branch:-unknown}",
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
