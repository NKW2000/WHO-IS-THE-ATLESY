package com.feudparty.core.network

import com.feudparty.core.game.Answer
import com.feudparty.core.game.BuzzState
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.TeamState
import org.junit.Assert.assertEquals
import org.junit.Test

class MessagesTest {
    @Test
    fun `buzz message round-trips through encode and decode`() {
        val original = ClientMessage.Buzz(playerId = "p-1", atMillis = 12345L)
        val encoded = encodeClientMessage(original)
        val decoded = decodeClientMessage(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `join message round-trips`() {
        val original = ClientMessage.Join(playerName = "سامر")
        val encoded = encodeClientMessage(original)
        val decoded = decodeClientMessage(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `assigned message round-trips`() {
        val original = HostMessage.Assigned(playerId = "p-2", teamId = TeamId.TEAM_2)
        val decoded = decodeHostMessage(encodeHostMessage(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `state update round-trips with the full game state`() {
        val state = GameState(
            questions = listOf(
                Question("q1", "سؤال", listOf(Answer("جواب", 50, revealed = true)), "عام")
            ),
            currentQuestionIndex = 0,
            teams = mapOf(
                TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١", score = 50, connected = true),
                TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢")
            ),
            buzzState = BuzzState.LOCKED_TEAM_1
        )
        val original = HostMessage.StateUpdate(state)
        val decoded = decodeHostMessage(encodeHostMessage(original))
        assertEquals(original, decoded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decoding a host message as a client message is rejected`() {
        decodeClientMessage(encodeHostMessage(HostMessage.Assigned("p-1", TeamId.TEAM_1)))
    }
}
