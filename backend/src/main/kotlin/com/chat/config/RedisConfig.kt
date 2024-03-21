package com.chat.config

import com.chat.config.properties.RedisCacheProperty
import io.lettuce.core.ClientOptions
import io.lettuce.core.SocketOptions
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@EnableCaching
@Configuration
class RedisConfig(
    private val redisCacheProperty: RedisCacheProperty
) {
    @Bean
    fun redisConfiguration(): RedisConfiguration {
        return RedisStandaloneConfiguration(redisCacheProperty.hostname)
    }

    @Bean
    @Primary
    fun redisConnectionFactory(redisConfiguration: RedisConfiguration): LettuceConnectionFactory {
        val clientConfigBuilder = LettuceClientConfiguration.builder()
        val socketOptions = SocketOptions.builder().connectTimeout(
            Duration.ofMillis(redisCacheProperty.connectionTimeoutMs)
        ).build()
        val clientOptions = ClientOptions.builder().socketOptions(socketOptions).build()
        clientConfigBuilder.clientOptions(clientOptions)
        clientConfigBuilder.commandTimeout(Duration.ofMillis(redisCacheProperty.commandTimeoutMs))
        val config = clientConfigBuilder.build()
        return LettuceConnectionFactory(redisConfiguration, config)
    }

    @Bean
    @Primary
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.setConnectionFactory(redisConnectionFactory)
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        return redisTemplate
    }
}
