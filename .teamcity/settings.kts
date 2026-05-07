import jetbrains.buildServer.configs.kotlin.*

version = "2025.11"

project {
    subProject(createStandardSubProject("Backend"))
    subProject(createStandardSubProject("Frontend"))
    subProject(createStandardSubProject("API"))
    subProject(createStandardSubProject("Mobile"))
}

fun createStandardSubProject(projectName: String) = Project {
    name = projectName
    id(name)
    StandardPipeline(name).registerIn(this)
}
