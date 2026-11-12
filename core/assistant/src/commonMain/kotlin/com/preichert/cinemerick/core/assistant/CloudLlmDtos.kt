package com.preichert.cinemerick.core.assistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// OpenAI-compatible chat completions: OpenAI, xAI, Mistral and OpenRouter speak the same shape.

@Serializable
internal data class ChatRequestDto(
    val model: String,
    val messages: List<ChatMessageDto>,
    @SerialName("response_format") val responseFormat: ResponseFormatDto = ResponseFormatDto(),
)

@Serializable
internal data class ChatMessageDto(val role: String, val content: String)

@Serializable
internal data class ResponseFormatDto(val type: String = "json_object")

@Serializable
internal data class ChatResponseDto(val choices: List<ChatChoiceDto> = emptyList())

@Serializable
internal data class ChatChoiceDto(val message: ChatMessageDto? = null)

// Anthropic Messages API.

@Serializable
internal data class AnthropicRequestDto(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ChatMessageDto>,
    @SerialName("output_config") val outputConfig: AnthropicOutputConfigDto? = null,
    // "default" routes a refused request to a fallback model chosen by the API.
    val fallbacks: String? = null,
)

@Serializable
internal data class AnthropicOutputConfigDto(val effort: String)

@Serializable
internal data class AnthropicResponseDto(
    val content: List<AnthropicBlockDto> = emptyList(),
    @SerialName("stop_reason") val stopReason: String? = null,
)

@Serializable
internal data class AnthropicBlockDto(val type: String, val text: String? = null)

// Google Gemini generateContent.

@Serializable
internal data class GeminiRequestDto(
    val systemInstruction: GeminiContentDto,
    val contents: List<GeminiContentDto>,
    val generationConfig: GeminiGenerationConfigDto = GeminiGenerationConfigDto(),
)

@Serializable
internal data class GeminiContentDto(val parts: List<GeminiPartDto>, val role: String? = null)

@Serializable
internal data class GeminiPartDto(val text: String? = null)

@Serializable
internal data class GeminiGenerationConfigDto(
    val responseMimeType: String = "application/json",
    val thinkingConfig: GeminiThinkingConfigDto? = null,
)

@Serializable
internal data class GeminiThinkingConfigDto(val thinkingBudget: Int)

@Serializable
internal data class GeminiResponseDto(val candidates: List<GeminiCandidateDto> = emptyList())

@Serializable
internal data class GeminiCandidateDto(val content: GeminiContentDto? = null)
