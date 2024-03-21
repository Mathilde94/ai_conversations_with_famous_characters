package com.chat.repository

import com.chat.model.ChatSessionModel
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class RedisChatRepository(redisTemplate: RedisTemplate<String, Any>) {
    private val valueOperations: ValueOperations<String, Any> = redisTemplate.opsForValue()

    fun save(chatSessionModel: ChatSessionModel) {
        valueOperations.set("$NAMESPACE:${chatSessionModel.chatId}", chatSessionModel, 1, TimeUnit.DAYS)
    }

    fun retrieve(chatSessionId: String): ChatSessionModel? {
        return valueOperations.get("$NAMESPACE:$chatSessionId") as ChatSessionModel?
    }

    companion object {
        const val NAMESPACE = "CHAT_SESSION"
    }
}
