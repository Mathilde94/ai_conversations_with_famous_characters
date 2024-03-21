package com.chat.service

import com.chat.controller.dto.PostChatCompletionRequest
import com.chat.model.ChatMessageModel
import com.chat.model.ChatSessionModel
import com.chat.repository.RedisChatRepository
import com.chat.service.llm.LLMInterfaceService
import com.chat.service.llm.Prompt
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.Date
import kotlin.test.assertEquals

class ChatServiceTests {
    private val redisChatRepository: RedisChatRepository = mockk<RedisChatRepository>()
    private val llmInterfaceService: LLMInterfaceService = mockk<LLMInterfaceService>()

    @BeforeEach
    fun setUp() {
        clearMocks(redisChatRepository, llmInterfaceService)
    }

    @Test
    fun `create chat session`() {
        val service = ChatService(redisChatRepository, llmInterfaceService)
        coEvery {
            redisChatRepository.save(any())
        } returns Unit

        val chatSessionModel = service.createChatSession("Jim", "Pam")
        assertEquals(chatSessionModel.simulatedCharacterName, "Jim")
        assertEquals(chatSessionModel.userCharacterName, "Pam")
    }

    @Test
    fun `get chat session`() {
        val service = ChatService(redisChatRepository, llmInterfaceService)
        val expectedChatSessionModel = ChatSessionModel("123")
        coEvery {
            redisChatRepository.retrieve("123")
        } returns expectedChatSessionModel

        val chatSessionModel = service.getChatSession("123")
        assertEquals(chatSessionModel, expectedChatSessionModel)
    }

    @Test
    fun `get clear chat session`() {
        val service = ChatService(redisChatRepository, llmInterfaceService)
        val model = ChatSessionModel("123")
        coEvery {
            redisChatRepository.save(model)
        } returns Unit
        service.resetChatSession(model)
        assertEquals(model.messages.size, 0)
    }

    @Test
    fun `add user message to session`() {
        val service = ChatService(redisChatRepository, llmInterfaceService)
        val initialChatSessionModel = ChatSessionModel("123", userCharacterName = "Jim")
        coEvery {
            redisChatRepository.save(any())
        } returns Unit

        val finalChatMessageModel = service.addUserMessageToSession(
            "hi there",
            initialChatSessionModel
        )

        assertEquals(finalChatMessageModel.role, "USER")
        assertEquals(finalChatMessageModel.content, "hi there")
        assertEquals(finalChatMessageModel.name, initialChatSessionModel.userCharacterName)
        assertEquals(initialChatSessionModel.messages.size, 1)
        assertEquals(initialChatSessionModel.messages.get(0), finalChatMessageModel)
    }

    @Test
    fun `add assistant message to session`() {
        val service = ChatService(redisChatRepository, llmInterfaceService)
        val initialChatSessionModel = ChatSessionModel("123", userCharacterName = "Jim", simulatedCharacterName = "Pam")
        initialChatSessionModel.messages.add(ChatMessageModel(
            role = "USER",
            content = "Hello",
            createdAtInS = Date().time,
            name = "Jim"
        ))

        coEvery {
            redisChatRepository.save(any())
        } returns Unit

        val finalChatMessageModel = service.addAssistantMessageToSession(
            "hi Jim",
            initialChatSessionModel
        )

        assertEquals(finalChatMessageModel.role, "ASSISTANT")
        assertEquals(finalChatMessageModel.content, "hi Jim")
        assertEquals(finalChatMessageModel.name, initialChatSessionModel.simulatedCharacterName)
        assertEquals(initialChatSessionModel.messages.size, 2)
        assertEquals(initialChatSessionModel.messages.get(1), finalChatMessageModel)
    }

    @Test
    fun `send chat message`() {
        val service = ChatService(redisChatRepository, llmInterfaceService)
        val chatSessionModel = ChatSessionModel("123",
            userCharacterName = "Jim",
            simulatedCharacterName = "Pam"
        )
        val llmStreamFlux = Flux.just("hi ", "there", "!")
        val prompt = Prompt.newBuilder()
            .setCurrentConversation(chatSessionModel.messages)
            .setToCharacter(chatSessionModel.simulatedCharacterName.toString())
            .build()
        val expectedPromptReq = PostChatCompletionRequest(prompt, stream = true)

        coEvery {
            llmInterfaceService.streamRequest(expectedPromptReq)
        } returns llmStreamFlux

        val fluxResponseMessages = service.sendChatMessage(chatSessionModel)

        StepVerifier.create(fluxResponseMessages)
            .expectNext("hi ")
            .expectNext("there")
            .expectNext("!")
            .verifyComplete()
    }
}
