package de.tolstun

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
// @EnableAsync
open class Application


fun main(args: Array<String>) {
    runApplication<Application>(*args)
}


