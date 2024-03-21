package com.chat.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "llm.server")
data class LLMServerConfig(
    val hostname: String = "localhost",
    val port: Int = 8080,
    val uri: String = "/completion"
)
