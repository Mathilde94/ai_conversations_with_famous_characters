package com.chat.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.protobuf.dto.chat.v1.Character
import com.protobuf.dto.chat.v1.ChatSession
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
data class ChatSessionModel @JsonCreator constructor(
    @JsonProperty("id") var chatId: String? = UUID.randomUUID().toString(),
    @JsonProperty("userCharacter") val userCharacterName: String? = null,
    @JsonProperty("simulatedCharacter") val simulatedCharacterName: String? = null,
    @JsonProperty("messages") var messages: MutableList<ChatMessageModel> = emptyList<ChatMessageModel>().toMutableList()
) {

    fun toProto(): ChatSession {
        val chatSession = ChatSession.newBuilder()
            .setUuid(chatId)
            .setCharacterSimulated(Character.newBuilder().setName(simulatedCharacterName).build())
            .setCharacterUser(Character.newBuilder().setName(userCharacterName).build())
            .addAllMessages(messages.map { it.toProto() }.toList())
        return chatSession.build()
    }

    companion object {
        fun fromProto(chatSession: ChatSession): ChatSessionModel {
            val messages = chatSession.messagesList.map {
                ChatMessageModel.fromProto(it)
            }.toMutableList()
            return ChatSessionModel(
                chatSession.uuid,
                chatSession.characterUser.name,
                chatSession.characterSimulated.name,
                messages
            )
        }
    }
}
