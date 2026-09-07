package com.feudparty.core.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** دور اللاعبين: مين بيقدر يضغط، ترتيب الدور، وألوان الشاشات. */
class GameEnginePlayersTest {

    @Test
    fun `only the podium player of each team may buzz in the face-off`() {
        val engine = GameEngine(freshState())
        assertEquals(setOf("a1", "b1"), engine.state.armedPlayerIds())

        val ignored = engine.buzz("a2")
        assertEquals(BuzzState.OPEN, ignored.buzzState)
        assertNull(ignored.buzzedPlayerId)

        val locked = engine.buzz("a1")
        assertEquals(BuzzState.LOCKED_TEAM_1, locked.buzzState)
        assertEquals("a1", locked.buzzedPlayerId)
        assertEquals(PlayerMark.BUZZED, locked.markFor("a1"))
    }

    @Test
    fun `control passes to the next team mate, not the one who just answered`() {
        val engine = GameEngine(freshState())
        val playing = engine.giveControlTo(TeamId.TEAM_1)

        // a1 جاوب بالمواجهة، فالدور بينتقل لـ a2.
        assertEquals("a2", playing.turnPlayerId)
        assertEquals(setOf("a2"), playing.armedPlayerIds())
        assertEquals(PlayerMark.ARMED, playing.markFor("a2"))
        assertEquals(PlayerMark.CORRECT, playing.markFor("a1"))
    }

    @Test
    fun `the team answers in a strict rotation`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)

        assertEquals("a2", engine.state.turnPlayerId)
        engine.correct(1)
        assertEquals("a3", engine.state.turnPlayerId)
        engine.wrong()
        assertEquals("a1", engine.state.turnPlayerId)
        engine.correct(2)
        assertEquals("a2", engine.state.turnPlayerId)
    }

    @Test
    fun `a wrong answer keeps the player red until the rotation comes back`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        engine.wrong() // a2 غلط

        assertEquals(PlayerMark.WRONG, engine.state.markFor("a2"))
        assertEquals("a3", engine.state.turnPlayerId)

        engine.wrong() // a3 غلط، الدور لـ a1
        assertEquals(PlayerMark.WRONG, engine.state.markFor("a2"))
        assertEquals("a1", engine.state.turnPlayerId)

        engine.correct(1) // a1 صح، الدور رجع لـ a2 فبتنمسح الحمرا
        assertEquals("a2", engine.state.turnPlayerId)
        assertEquals(PlayerMark.ARMED, engine.state.markFor("a2"))
        assertFalse("a2" in engine.state.wrongPlayers)
    }

    @Test
    fun `a player who is not on turn cannot answer`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        val result = engine.buzz("a3") // مش دوره

        assertNull(result.buzzedPlayerId)
        assertEquals(PlayerMark.IDLE, result.markFor("a3"))
    }

    @Test
    fun `the player on turn can press to show they are answering`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        val result = engine.buzz("a2")

        assertEquals("a2", result.buzzedPlayerId)
        assertEquals(PlayerMark.BUZZED, result.markFor("a2"))
    }

    @Test
    fun `the steal goes to the podium player of the other team`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        repeat(3) { engine.wrong() }

        assertEquals(RoundPhase.STEAL, engine.state.phase)
        assertEquals("b1", engine.state.turnPlayerId)
        assertEquals(setOf("b1"), engine.state.armedPlayerIds())
    }

    @Test
    fun `the face-off pairs the same seat number from each team`() {
        val state = freshState()

        TeamId.entries.forEach { team ->
            assertEquals(1, state.podiumPlayer(team)?.seat)
        }
        val samer = state.playersOf(TeamId.TEAM_1).first()
        assertEquals("b1", state.opponentOf(samer)?.id)
        assertEquals(samer.seat, state.opponentOf(samer)?.seat)
    }

    @Test
    fun `a short team wraps so there is always an opponent`() {
        val uneven = listOf(
            Player("a1", "أ١", TeamId.TEAM_1, seat = 1),
            Player("a2", "أ٢", TeamId.TEAM_1, seat = 2),
            Player("b1", "ب١", TeamId.TEAM_2, seat = 1)
        )
        val state = freshState(players = uneven).copy(faceOffSeat = 2)

        assertEquals("a2", state.podiumPlayer(TeamId.TEAM_1)?.id)
        // الفريق التاني فيه لاعب واحد، فبيرجع عليه.
        assertEquals("b1", state.podiumPlayer(TeamId.TEAM_2)?.id)
    }

    @Test
    fun `the podium player changes every round`() {
        val engine = GameEngine(freshState())
        assertEquals("a1", engine.state.podiumPlayer(TeamId.TEAM_1)?.id)
        assertEquals("b1", engine.state.podiumPlayer(TeamId.TEAM_2)?.id)

        engine.giveControlTo(TeamId.TEAM_1)
        repeat(3) { engine.wrong() }
        engine.wrong() // انتهت الجولة
        engine.apply(GameEvent.NextRound) // شاشة النتائج
        val next = engine.apply(GameEvent.NextRound)

        assertEquals(2, next.faceOffSeat)
        assertEquals("a2", next.podiumPlayer(TeamId.TEAM_1)?.id)
        assertEquals("b2", next.podiumPlayer(TeamId.TEAM_2)?.id)
        assertEquals(setOf("a2", "b2"), next.armedPlayerIds())
        assertTrue(next.wrongPlayers.isEmpty())
    }

    @Test
    fun `joining marks the team connected and keeps the join order`() {
        val engine = GameEngine(freshState(players = emptyList()))
        engine.apply(GameEvent.PlayerJoined("p1", "سامر", TeamId.TEAM_1))
        engine.apply(GameEvent.PlayerJoined("p2", "ليلى", TeamId.TEAM_2))
        val result = engine.apply(GameEvent.PlayerJoined("p3", "رامي", TeamId.TEAM_1))

        assertEquals(listOf("p1", "p3"), result.playersOf(TeamId.TEAM_1).map { it.id })
        // كل لاعب بياخد رقمه بفريقه لما ينضم.
        assertEquals(listOf(1, 2), result.playersOf(TeamId.TEAM_1).map { it.seat })
        assertEquals(listOf(1), result.playersOf(TeamId.TEAM_2).map { it.seat })
        assertEquals("سامر", result.podiumPlayer(TeamId.TEAM_1)?.name)
        assertTrue(result.teams.getValue(TeamId.TEAM_1).connected)
        assertTrue(result.teams.getValue(TeamId.TEAM_2).connected)
    }

    @Test
    fun `a disconnected player is skipped in the rotation`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1) // الدور صار على a2
        engine.apply(GameEvent.PlayerLeft("a3"))
        val result = engine.correct(1) // a2 جاوب، المفروض نتخطى a3

        assertEquals("a1", result.turnPlayerId)
    }

    @Test
    fun `losing every player marks the team disconnected`() {
        val engine = GameEngine(freshState())
        engine.apply(GameEvent.PlayerLeft("a1"))
        engine.apply(GameEvent.PlayerLeft("a2"))
        val result = engine.apply(GameEvent.PlayerLeft("a3"))

        assertFalse(result.teams.getValue(TeamId.TEAM_1).connected)
        assertTrue(result.teams.getValue(TeamId.TEAM_2).connected)
    }

    @Test
    fun `a single-player team keeps its turn`() {
        val solo = listOf(
            Player("a1", "وحيد", TeamId.TEAM_1, seat = 1),
            Player("b1", "وحيدة", TeamId.TEAM_2, seat = 1)
        )
        val engine = GameEngine(freshState(players = solo))
        engine.giveControlTo(TeamId.TEAM_1)

        assertEquals("a1", engine.state.turnPlayerId)
        engine.correct(1)
        assertEquals("a1", engine.state.turnPlayerId)
    }

    @Test
    fun `a team that loses everybody mid round does not freeze the game`() {
        val engine = GameEngine(freshState())
        engine.giveControlTo(TeamId.TEAM_1)
        listOf("a1", "a2", "a3").forEach { engine.apply(GameEvent.PlayerLeft(it)) }

        // ما ضل حدا يضغط، بس المضيف لسا بيقدر يحكم ويكمّل الجولة.
        val afterWrong = engine.wrong()
        assertEquals(RoundPhase.PLAY, afterWrong.phase)

        engine.wrong()
        val steal = engine.wrong()
        assertEquals(RoundPhase.STEAL, steal.phase)
        assertEquals("b1", steal.turnPlayerId)

        val ended = engine.correct(1)
        assertEquals(RoundPhase.ROUND_END, ended.phase)
        assertEquals(TeamId.TEAM_2, ended.roundWinner)
    }

    @Test
    fun `one player per team plays a whole round`() {
        val solo = listOf(
            Player("a1", "وحيد", TeamId.TEAM_1, seat = 1),
            Player("b1", "وحيدة", TeamId.TEAM_2, seat = 1)
        )
        val engine = GameEngine(freshState(players = solo))

        assertEquals(setOf("a1", "b1"), engine.state.armedPlayerIds())
        engine.buzzPodium(TeamId.TEAM_1)
        engine.correct(0)
        engine.choosePlay()

        // نفس اللاعب بيضل دوره لأنه ما في غيره بالفريق.
        assertEquals("a1", engine.state.turnPlayerId)
        engine.correct(1)
        assertEquals("a1", engine.state.turnPlayerId)
        repeat(3) { engine.wrong() }
        assertEquals(RoundPhase.STEAL, engine.state.phase)
        assertEquals("b1", engine.state.turnPlayerId)
    }

    @Test
    fun `moving a player renumbers both teams`() {
        val engine = GameEngine(freshState())
        val moved = engine.apply(GameEvent.PlayerMoved("a2", TeamId.TEAM_2))

        assertEquals(listOf(1, 2), moved.playersOf(TeamId.TEAM_1).map { it.seat })
        assertEquals(listOf("a1", "a3"), moved.playersOf(TeamId.TEAM_1).map { it.id })
        assertEquals(listOf(1, 2, 3, 4), moved.playersOf(TeamId.TEAM_2).map { it.seat })
        assertTrue(moved.playersOf(TeamId.TEAM_2).any { it.id == "a2" })
    }

    @Test
    fun `the match start flag reaches the players`() {
        val engine = GameEngine(freshState())
        assertFalse(engine.state.matchStarted)

        val started = engine.apply(GameEvent.StartGame)
        assertTrue(started.matchStarted)
        assertTrue(started.maskedForPlayers().matchStarted)
    }
}
