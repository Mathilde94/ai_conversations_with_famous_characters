package com.chat.service.llm

import com.chat.config.properties.LLMServerConfig
import com.chat.controller.dto.PostChatCompletionRequest
import com.chat.model.ChatSessionModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class LLMInterfaceServiceTests {
    private val llmWebClientInterface: LLMWebClientInterface = mockk<LLMWebClientInterface>()
    private val config = LLMServerConfig()
    private val llmInterfaceService = LLMInterfaceService(llmWebClientInterface, config)
    private val chatSessionModel = ChatSessionModel("123",
        userCharacterName = "Jim",
        simulatedCharacterName = "Pam"
    )

    @Test
    fun `stream request to LLM with no new line`() {
        val llmStreamFlux = buildLLMJSONResponseAsFlux(listOf("hi ", "there", "!"))
        val prompt = Prompt.newBuilder()
            .setCurrentConversation(chatSessionModel.messages)
            .setToCharacter(chatSessionModel.simulatedCharacterName.toString())
            .build()
        val completionRequest = PostChatCompletionRequest(prompt, stream = true)

        coEvery { llmWebClientInterface.getClient() } returns mockWebClientResponse(config, completionRequest, llmStreamFlux)

        val response = llmInterfaceService.streamRequest(completionRequest)
        StepVerifier.create(response)
            .expectNext("hi ")
            .expectNext("there")
            .expectNext("!")
            .verifyComplete()
    }

    @Test
    fun `stream request to LLM with new line`() {
        val llmStreamFlux = buildLLMJSONResponseAsFlux(listOf("hi ", "\\n How", " are you?"))
        val prompt = Prompt.newBuilder()
            .setCurrentConversation(chatSessionModel.messages)
            .setToCharacter(chatSessionModel.simulatedCharacterName.toString())
            .build()
        val completionRequest = PostChatCompletionRequest(prompt, stream = true)

        coEvery { llmWebClientInterface.getClient() } returns mockWebClientResponse(config, completionRequest, llmStreamFlux)

        val response = llmInterfaceService.streamRequest(completionRequest)
        StepVerifier.create(response)
            .expectNext("hi ")
            .verifyComplete()
    }

    @Test
    fun `stream request to LLM with new line allowed`() {
        val llmStreamFlux = buildLLMJSONResponseAsFlux(listOf("hi ", "\\n How", " are you?"))
        val prompt = Prompt.newBuilder()
            .setCurrentConversation(chatSessionModel.messages)
            .setToCharacter(chatSessionModel.simulatedCharacterName.toString())
            .build()
        val completionRequest = PostChatCompletionRequest(prompt, stream = true)

        coEvery { llmWebClientInterface.getClient() } returns mockWebClientResponse(config, completionRequest, llmStreamFlux)

        val response = llmInterfaceService.streamRequest(completionRequest, stopAtFirstLine = false)
        StepVerifier.create(response)
            .expectNext("hi ")
            .expectNext("\\n How")
            .expectNext(" are you?")
            .verifyComplete()
    }

    private fun mockWebClientResponse(config: LLMServerConfig, completionRequest: PostChatCompletionRequest, llmStreamFlux: Flux<DataBuffer>): WebClient {
        val mockClient = mockk<WebClient>()
        val uriSpec = mockk<RequestBodyUriSpec>()
        val requestBodySpec = mockk<RequestBodySpec>()
        val requestHeaderSpec = mockk<RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val dataBuffer = llmStreamFlux

        coEvery { mockClient.post() } returns uriSpec
        coEvery { uriSpec.uri(config.uri) } returns requestBodySpec
        coEvery { requestBodySpec.bodyValue(completionRequest) } returns requestHeaderSpec
        coEvery { requestHeaderSpec.retrieve() } returns responseSpec
        coEvery { responseSpec.bodyToFlux(DataBuffer::class.java) } returns dataBuffer

        return mockClient
    }

    private fun buildLLMJSONResponseAsFlux(values: List<String>): Flux<DataBuffer> {
        val jsonList = values.map {
            "{\"content\": \"$it\"}"
        }
        val llmStreamFlux = Flux.just(*jsonList.toTypedArray())
            .map {
                val bufferFactory = DefaultDataBufferFactory()
                val bytes = it.toByteArray()
                val buffer = bufferFactory.allocateBuffer(bytes.size)
                buffer.write(bytes)
                buffer as DataBuffer
            }
        return llmStreamFlux
    }
}
