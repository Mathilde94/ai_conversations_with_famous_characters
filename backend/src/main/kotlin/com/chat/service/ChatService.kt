package com.chat.service

import com.chat.controller.dto.PostChatCompletionRequest
import com.chat.model.ChatMessageModel
import com.chat.model.ChatSessionModel
import com.chat.repository.RedisChatRepository
import com.chat.service.llm.LLMInterfaceService
import com.chat.service.llm.Prompt
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.Date
import java.util.UUID

@Component
class ChatService(
    val redisChatRepository: RedisChatRepository,
    val llmInterfaceService: LLMInterfaceService
) {
    fun createChatSession(simulatedCharacterName: String, userCharacterName: String): ChatSessionModel {
        val chatSessionModel = ChatSessionModel(
            UUID.randomUUID().toString(),
            userCharacterName,
            simulatedCharacterName
        )
        redisChatRepository.save(chatSessionModel)
        return chatSessionModel
    }

    fun getChatSession(chatSessionId: String): ChatSessionModel? {
        return redisChatRepository.retrieve(chatSessionId)
    }

    fun resetChatSession(chatSessionModel: ChatSessionModel) {
        chatSessionModel.messages.clear()
        redisChatRepository.save(chatSessionModel)
    }

    fun sendChatMessage(chatSessionModel: ChatSessionModel): Flux<String?> {
        // Enrich here with past session etc
        val prompt = Prompt.newBuilder()
            .setCurrentConversation(chatSessionModel.messages)
            .setToCharacter(chatSessionModel.simulatedCharacterName.toString())
            .build()
        println("Prompt to send: $prompt")
        val postChatCompletionRequest = PostChatCompletionRequest(prompt, stream = true)
        val fluxLLM = llmInterfaceService.streamRequest(postChatCompletionRequest)
        return fluxLLM.filter { !it.isNullOrEmpty() }
    }

    fun addUserMessageToSession(userContent: String, chatSessionModel: ChatSessionModel): ChatMessageModel {
        val chatMessageModel = ChatMessageModel(
            "USER",
            userContent,
            Date().time,
            chatSessionModel.userCharacterName
        )
        chatSessionModel.messages.add(chatMessageModel)
        redisChatRepository.save(chatSessionModel)
        return chatMessageModel
    }

    fun addAssistantMessageToSession(assistantContent: String, chatSessionModel: ChatSessionModel): ChatMessageModel {
        val chatMessageModel = ChatMessageModel(
            "ASSISTANT",
            assistantContent,
            Date().time,
            chatSessionModel.simulatedCharacterName
        )
        chatSessionModel.messages.add(chatMessageModel)
        redisChatRepository.save(chatSessionModel)
        return chatMessageModel
    }
}
