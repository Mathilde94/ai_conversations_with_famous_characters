package com.chat.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.protobuf.Timestamp
import com.protobuf.dto.chat.v1.Character
import com.protobuf.dto.chat.v1.ChatMessage

data class ChatMessageModel @JsonCreator constructor(
    @JsonProperty("role") var role: String,
    @JsonProperty("content") val content: String,
    @JsonProperty("createdAt") val createdAtInS: Long,
    @JsonProperty("character") var name: String? = ""
) {

    fun toProto(): ChatMessage {
        val chatMessage = ChatMessage.newBuilder()
            .setRole(role)
            .setContent(content)
            .setCharacter(Character.newBuilder().setName(name).build())
            .setCreatedAt(Timestamp.newBuilder().setSeconds(createdAtInS).build())
        return chatMessage.build()
    }
    companion object {
        fun fromProto(chatMessage: ChatMessage): ChatMessageModel {
            return ChatMessageModel(
                chatMessage.role,
                chatMessage.content,
                chatMessage.createdAt.seconds,
                chatMessage.character.name
            )
        }
    }
}
