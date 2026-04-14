package co.jarias.flexapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform