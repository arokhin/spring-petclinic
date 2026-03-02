import jetbrains.buildServer.configs.kotlin.*

version = "2025.11"

project {

    subProject {
        id("Backend_Project")
        name = "Backend"
        StandardPipeline("Backend").registerIn(this)
    }


}
