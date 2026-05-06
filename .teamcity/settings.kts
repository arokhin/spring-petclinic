import jetbrains.buildServer.configs.kotlin.*
import templates.UnitTestsTemplate

version = "2024.12"

project {
    template(UnitTestsTemplate)
}
