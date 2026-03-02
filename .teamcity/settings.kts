import jetbrains.buildServer.configs.kotlin.*

version = "2025.11"

project {

    subProject {
        name = "Backend"
        StandardPipeline("MyService").registerIn(this)
    }

}
