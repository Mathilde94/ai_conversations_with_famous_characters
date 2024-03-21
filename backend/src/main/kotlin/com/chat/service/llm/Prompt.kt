package com.chat.service.llm

import com.chat.model.ChatMessageModel

data class Prompt(
    var prompt: String,
    val currentConversation: String = "",
    val toCharacterName: String = ""
) {

    fun build(): String {
        prompt = prompt
            .replace("{currentConversation}", currentConversation)
            .replace("{toCharacterName}", toCharacterName)
        return prompt
    }

    fun setCurrentConversation(chatMessages: List<ChatMessageModel>): Prompt {
        return Prompt(
            prompt,
            currentConversation = chatMessages.map { "${it.name}: ${it.content}" }.joinToString("\n"),
            toCharacterName = toCharacterName
        )
    }

    fun setToCharacter(characterName: String): Prompt {
        return Prompt(
            prompt,
            currentConversation = currentConversation,
            toCharacterName = characterName
        )
    }

    companion object {
        const val CONVERSATION_PROMPT = """Below is an instruction that describes a task, paired with an input that provides further context.
Write a response that appropriately completes the request.

Instruction:
Given the beginning of a conversation between two movie characters, provide a line of dialogue that continues the conversation.

Context of the current conversation:
{currentConversation}
{toCharacterName}:"""

        fun newBuilder(): Prompt {
            return Prompt(CONVERSATION_PROMPT)
        }
    }
}
