package com.chat.service.llm

import com.chat.config.properties.LLMServerConfig
import com.chat.controller.dto.PostChatCompletionRequest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.nio.charset.StandardCharsets

@Component
class LLMInterfaceService(
    val lLMWebClientInterface: LLMWebClientInterface,
    val llmServerConfig: LLMServerConfig
) {

    fun streamRequest(postChatCompletionRequest: PostChatCompletionRequest, stopAtFirstLine: Boolean = true): Flux<String> {
        var wordPostFirstLine = false
//        return Flux.just("hi ", "there", "!", "How ", "hav", "e you", " been", " doin", "g?")
        return lLMWebClientInterface.getClient()
            .post()
            .uri(llmServerConfig.uri)
            .bodyValue(postChatCompletionRequest)
            .retrieve()
            .bodyToFlux(DataBuffer::class.java)
            .mapNotNull { extractUserResponseText(it) }
            .mapNotNull {
                if (it.contains("\\n")) { wordPostFirstLine = true }
                it
            }
            .mapNotNull { if (!stopAtFirstLine || !wordPostFirstLine) it else null }
    }

    private fun extractUserResponseText(dataBuffer: DataBuffer): String {
        val mapper = ObjectMapper()
        val stringValue = dataBuffer.toString(StandardCharsets.UTF_8)
        val newValue = stringValue.replace("data: ", "")
        val newJsonValue = mapper.readValue(newValue, JsonNode::class.java)
        val contentValue = newJsonValue.get("content").toString().replace("\\\"", "")
        return contentValue.trim('"')
    }
}
