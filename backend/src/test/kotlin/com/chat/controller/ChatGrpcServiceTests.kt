package com.chat.controller

import com.chat.controller.grpc.ChatGrpcService
import com.chat.model.ChatMessageModel
import com.chat.model.ChatSessionModel
import com.chat.service.ChatService
import com.protobuf.dto.chat.v1.Character
import com.protobuf.dto.chat.v1.ChatMessage
import com.protobuf.rpc.chat.v1.InitiateChatSessionRequest
import com.protobuf.rpc.chat.v1.GetChatSessionRequest
import com.protobuf.rpc.chat.v1.ResetChatSessionRequest
import com.protobuf.rpc.chat.v1.SendMessageStreamRequest
import com.protobuf.rpc.chat.v1.SendMessageStreamResponse
import io.grpc.Status
import io.grpc.StatusException
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChatGrpcServiceTests {
    private val chatService: ChatService = mockk<ChatService>()

    private val simulatedCharacterName = "Pam"
    private val userCharacterName = "Jim"

    private val simulatedCharacter = Character.newBuilder()
        .setName(simulatedCharacterName)
        .build()
    private val userCharacter = Character.newBuilder()
        .setName(userCharacterName)
        .build()

    @BeforeEach
    fun setUp() {
        clearMocks(chatService)
    }

    @Test
    fun `initialize chat session`() {
        val service = ChatGrpcService(chatService)

        coEvery {
            chatService.createChatSession(simulatedCharacterName, userCharacterName)
        } returns ChatSessionModel("123", userCharacterName, simulatedCharacterName)

        val request = InitiateChatSessionRequest.newBuilder()
            .setCharacterUser(userCharacter)
            .setCharacterSimulated(simulatedCharacter)
            .build()
        val initiateChatSessionResponse = runBlocking {
            service.initiateChatSession(request)
        }

        assertEquals(initiateChatSessionResponse.chatSession.uuid, "123")
        assertEquals(initiateChatSessionResponse.chatSession.characterUser.name, userCharacterName)
        assertEquals(initiateChatSessionResponse.chatSession.characterSimulated.name, simulatedCharacterName)
    }

    @Test
    fun `get chat session`() {
        val service = ChatGrpcService(chatService)

        coEvery {
            chatService.getChatSession("123")
        } returns ChatSessionModel("123", userCharacterName, simulatedCharacterName)

        val request = GetChatSessionRequest.newBuilder()
            .setChatSessionId("123")
            .build()
        val initiateChatSessionResponse = runBlocking {
            service.getChatSession(request)
        }

        assertEquals(initiateChatSessionResponse.chatSession.uuid, "123")
        assertEquals(initiateChatSessionResponse.chatSession.characterUser.name, userCharacterName)
        assertEquals(initiateChatSessionResponse.chatSession.characterSimulated.name, simulatedCharacterName)
    }
    @Test
    fun `get reset session`() {
        val service = ChatGrpcService(chatService)
        val model = ChatSessionModel("123", userCharacterName, simulatedCharacterName)
        model.messages.add(ChatMessageModel("USER", "Hi there!", Date().time, userCharacterName))

        coEvery {
            chatService.getChatSession("123")
        } returns model

        coEvery {
            chatService.resetChatSession(model)
        } returns runBlocking {
            model.messages.clear()
        }

        val request = ResetChatSessionRequest.newBuilder()
            .setChatSessionId("123")
            .build()
        val resetChatSessionResponse = runBlocking {
            service.resetChatSession(request)
        }

        assertEquals(resetChatSessionResponse.chatSession.uuid, "123")
        assertEquals(resetChatSessionResponse.chatSession.characterUser.name, userCharacterName)
        assertEquals(resetChatSessionResponse.chatSession.characterSimulated.name, simulatedCharacterName)
        assertEquals(resetChatSessionResponse.chatSession.messagesList.size, 0)
    }

    @Test
    fun `get not found chat session`() {
        val service = ChatGrpcService(chatService)

        coEvery {
            chatService.getChatSession("non-existing-chat-session")
        } returns null

        val request = GetChatSessionRequest.newBuilder()
            .setChatSessionId("non-existing-chat-session")
            .build()
        val exception = assertFailsWith<StatusException> {
            runBlocking {
                service.getChatSession(request)
            }
        }
        assertEquals(exception.status, Status.NOT_FOUND)
    }

    @Test
    fun `receive message stream`() {
        val service = ChatGrpcService(chatService)
        val request = SendMessageStreamRequest.newBuilder()
            .setMessage(ChatMessage.newBuilder().setContent("Hi there!").build())
            .setChatSessionId("123")
            .build()
        val chatSessionModel = ChatSessionModel("123", userCharacterName, simulatedCharacterName)
        val userChatModel = ChatMessageModel(
            role = "USER",
            content = request.message.content,
            createdAtInS = Date().time
        )
        val expectedChatAssistantModel = ChatMessageModel(
            role = "ASSISTANT",
            content = "Hi there!",
            createdAtInS = Date().time
        )
        val llmStreamFlux = Flux.just("hi ", "there", "!")

        coEvery {
            chatService.getChatSession("123")
        } returns chatSessionModel

        coEvery {
            chatService.addUserMessageToSession(request.message.content, chatSessionModel)
        } returns userChatModel

        coEvery {
            chatService.sendChatMessage(chatSessionModel)
        } returns llmStreamFlux

        coEvery {
            chatService.addAssistantMessageToSession("hi there!", any())
        } returns expectedChatAssistantModel

        val result = runBlocking {
            service.sendMessageStream(request)
        }

        StepVerifier.create(result.asFlux())
            .expectNext(SendMessageStreamResponse.newBuilder().setChunkMessage("hi ").build())
            .expectNext(SendMessageStreamResponse.newBuilder().setChunkMessage("there").build())
            .expectNext(SendMessageStreamResponse.newBuilder().setChunkMessage("!").build())
            .verifyComplete()
    }
}
