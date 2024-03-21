package com.chat

import lombok.Generated
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@Generated
@SpringBootApplication
@ConfigurationPropertiesScan
class Application

@Generated
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
