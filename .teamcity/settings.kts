import jetbrains.buildServer.configs.kotlin.*

version = "2025.11"

project {

    subProject {
        name = "Backend"
        StandardPipeline("Backend").registerIn(this)
    }


    subProject {
        name = "Frontend"
        StandardPipeline("Frontend").registerIn(this)
    }
}
