package com.chat.controller.dto

data class PostChatCompletionRequest(
    val prompt: String,
    val temperature: Float = 0.9F,
    val n_predict: Int = 100,
    val stream: Boolean = false
)
