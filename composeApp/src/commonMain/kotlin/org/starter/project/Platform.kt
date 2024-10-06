package org.starter.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform