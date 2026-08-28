package com.feudparty.core.network

import com.feudparty.core.game.GameState
import com.feudparty.core.game.TeamId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/** رسائل من جهاز الفريق ← للمضيف. */
@Serializable
sealed class ClientMessage {
    @Serializable
    @SerialName("join")
    data class Join(val teamName: String) : ClientMessage()

    @Serializable
    @SerialName("buzz")
    data class Buzz(val teamId: TeamId, val atMillis: Long) : ClientMessage()
}

/** رسائل من المضيف ← لأجهزة الفرق. المضيف هو مصدر الحقيقة الوحيد. */
@Serializable
sealed class HostMessage {
    @Serializable
    @SerialName("state")
    data class StateUpdate(val state: GameState) : HostMessage()

    @Serializable
    @SerialName("assigned")
    data class Assigned(val teamId: TeamId) : HostMessage()
}

fun encodeClientMessage(message: ClientMessage): ByteArray =
    json.encodeToString(ClientMessage.serializer(), message).encodeToByteArray()

fun decodeClientMessage(bytes: ByteArray): ClientMessage =
    json.decodeFromString(ClientMessage.serializer(), bytes.decodeToString())

fun encodeHostMessage(message: HostMessage): ByteArray =
    json.encodeToString(HostMessage.serializer(), message).encodeToByteArray()

fun decodeHostMessage(bytes: ByteArray): HostMessage =
    json.decodeFromString(HostMessage.serializer(), bytes.decodeToString())
