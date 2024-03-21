package com.chat.service.llm

import com.chat.config.properties.LLMServerConfig
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class LLMWebClientInterface(val llmServerConfig: LLMServerConfig) {
    fun getClient(): WebClient {
        return WebClient.create(
            "http://${llmServerConfig.hostname}:${llmServerConfig.port}"
        )
    }
}
