package com.chat.service.llm

import com.chat.model.ChatMessageModel
import com.chat.model.ChatSessionModel
import org.junit.jupiter.api.Test
import java.util.Date
import kotlin.test.assertTrue

class PromptTests {
    @Test
    fun `create right prompt formatting`() {
        val chatSessionModel = ChatSessionModel("123",
            userCharacterName = "Jim",
            simulatedCharacterName = "Pam"
        )
        chatSessionModel.messages.add(
            ChatMessageModel(
                role = "USER",
                content = "Hello",
                createdAtInS = Date().time,
                name = "Jim"
            )
        )
        val prompt = Prompt.newBuilder()
            .setCurrentConversation(chatSessionModel.messages)
            .setToCharacter(chatSessionModel.simulatedCharacterName.toString())
            .build()

        assertTrue { prompt.contains("Instruction:") }
        assertTrue { prompt.contains("Jim: Hello") }
        assertTrue { prompt.contains("Pam:") }
    }
}
