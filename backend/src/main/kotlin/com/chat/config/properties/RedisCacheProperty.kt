package com.chat.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.cache.redis")
data class RedisCacheProperty(
    val hostname: String = "localhost",
    val port: Int = 6379,
    val connectionTimeoutMs: Long = 10000,
    val commandTimeoutMs: Long = 500,
    val readTimeoutMs: Long = 1000
)
