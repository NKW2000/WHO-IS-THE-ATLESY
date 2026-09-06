package com.feudparty.app.ui

import com.feudparty.core.game.GameState
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.buzzedTeam

internal fun GameState.nameOf(teamId: TeamId?): String =
    teamId?.let { teams[it]?.name } ?: "الفريق"

private fun GameState.playerName(playerId: String?): String =
    player(playerId)?.name ?: "اللاعب"

/** نص الحالة عند المضيف — دايماً بيقول له شو المطلوب منه هلق. */
fun hostStatusText(state: GameState): String = when (state.phase) {
    RoundPhase.FACE_OFF -> when (state.buzzedTeam()) {
        null -> {
            val one = state.podiumPlayer(TeamId.TEAM_1)?.name ?: "—"
            val two = state.podiumPlayer(TeamId.TEAM_2)?.name ?: "—"
            "مواجهة رقم ${state.faceOffSeat.ar()}: $one ضد $two"
        }

        else -> "${state.playerName(state.buzzedPlayerId)} ضغط أول — احكم على جوابه"
    }

    RoundPhase.FACE_OFF_SECOND -> {
        val player = state.faceOffTeam?.let { state.podiumPlayer(it)?.name } ?: "الفريق التاني"
        "دور $player بالمواجهة — لازم جواب أعلى"
    }

    RoundPhase.PLAY_OR_PASS -> {
        val team = state.nameOf(state.faceOffWinner)
        "$team كسب المواجهة — بيختار يلعب أو يمرّر"
    }

    RoundPhase.SCOREBOARD -> "النتيجة — اضغط لتبلّش الجولة الجاية"

    RoundPhase.PLAY ->
        "دور ${state.playerName(state.turnPlayerId)} — ${state.strikes.ar()}/${state.strikesToSteal.ar()} أخطاء"

    RoundPhase.STEAL ->
        "سرقة: ${state.playerName(state.turnPlayerId)} عنده جواب واحد"

    RoundPhase.ROUND_END -> "انتهت الجولة — انتقل للجولة الجاية"
    RoundPhase.GAME_OVER -> "انتهت اللعبة"
}
