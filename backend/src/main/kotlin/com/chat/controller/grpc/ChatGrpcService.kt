package com.chat.controller.grpc
import com.chat.service.ChatService
import com.protobuf.rpc.chat.v1.*
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.reactive.asFlow
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class ChatGrpcService(
    private val chatService: ChatService
) : ChatServiceGrpcKt.ChatServiceCoroutineImplBase() {

    override suspend fun initiateChatSession(request: InitiateChatSessionRequest): InitiateChatSessionResponse {
        val chatSessionModel = chatService.createChatSession(
            request.characterSimulated.name,
            request.characterUser.name
        )
        return InitiateChatSessionResponse.newBuilder()
            .setChatSession(chatSessionModel.toProto())
            .build()
    }

    override suspend fun getChatSession(request: GetChatSessionRequest): GetChatSessionResponse {
        when (val chatSessionModel = chatService.getChatSession(request.chatSessionId)) {
            null -> throw StatusException(Status.NOT_FOUND)
            else -> return GetChatSessionResponse.newBuilder()
                .setChatSession(chatSessionModel.toProto())
                .build()
        }
    }

    override suspend fun resetChatSession(request: ResetChatSessionRequest): ResetChatSessionResponse {
        when (val chatSessionModel = chatService.getChatSession(request.chatSessionId)) {
            null -> throw StatusException(Status.NOT_FOUND)
            else -> {
                chatService.resetChatSession(chatSessionModel)
                return ResetChatSessionResponse.newBuilder()
                    .setChatSession(chatSessionModel.toProto())
                    .build()
            }
        }
    }

    override fun sendMessageStream(request: SendMessageStreamRequest): Flow<SendMessageStreamResponse> {
        val chatSessionModel = chatService.getChatSession(request.chatSessionId)
        if (chatSessionModel === null) {
            throw StatusRuntimeException(Status.NOT_FOUND.withDescription("Invalid session id"))
        }
        chatService.addUserMessageToSession(request.message.content, chatSessionModel)
        val results = mutableListOf<String?>()
        return chatService.sendChatMessage(chatSessionModel)
            .map {
                results.add(it)
                Thread.sleep(100) // to look more "human"
                SendMessageStreamResponse.newBuilder().setChunkMessage(it).build()
            }
            .asFlow()
            .onCompletion {
                chatService.addAssistantMessageToSession(
                    results.map { it.toString() }.joinToString(""),
                    chatSessionModel
                )
            }
    }
}
