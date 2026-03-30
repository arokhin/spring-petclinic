import jetbrains.buildServer.configs.kotlin.*

version = "2025.11"

project {

    subProject {
        id("Backend_Project")
        name = "Backend"
        StandardPipeline("Backend").registerIn(this)
    }

    subProject {
        id("Frontend_Project")
        name = "Frontend"
        StandardPipeline("Frontend").registerIn(this)
    }

    subProject {
        id("API_Project")
        name = "API"
        StandardPipeline("API").registerIn(this)
    }
}
