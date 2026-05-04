import jetbrains.buildServer.configs.kotlin.*
import templates.*
import tests.*

version = "2024.12"

project {
    template(TestsTemplate)
    buildType(ServiceTests)
}
