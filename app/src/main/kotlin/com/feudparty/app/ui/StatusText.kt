package com.feudparty.app.ui

import com.feudparty.core.game.GameState
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.buzzedTeam

private fun GameState.nameOf(teamId: TeamId?): String =
    teamId?.let { teams[it]?.name } ?: "الفريق"

private fun GameState.playerName(playerId: String?): String =
    player(playerId)?.name ?: "اللاعب"

/** نص الحالة عند المضيف — دايماً بيقول له شو المطلوب منه هلق. */
fun hostStatusText(state: GameState): String = when (state.phase) {
    RoundPhase.FACE_OFF -> when (state.buzzedTeam()) {
        null -> {
            val one = state.podiumPlayer(TeamId.TEAM_1)?.name ?: "—"
            val two = state.podiumPlayer(TeamId.TEAM_2)?.name ?: "—"
            "المواجهة: $one ضد $two"
        }

        else -> "${state.playerName(state.buzzedPlayerId)} ضغط أول — احكم على جوابه"
    }

    RoundPhase.FACE_OFF_SECOND -> {
        val player = state.faceOffTeam?.let { state.podiumPlayer(it)?.name } ?: "الفريق التاني"
        "دور $player بالمواجهة — لازم جواب أعلى"
    }

    RoundPhase.PLAY ->
        "دور ${state.playerName(state.turnPlayerId)} — ${state.strikes}/3 أخطاء"

    RoundPhase.STEAL ->
        "سرقة: ${state.playerName(state.turnPlayerId)} عنده جواب واحد"

    RoundPhase.ROUND_END -> "انتهت الجولة — انتقل للجولة الجاية"
    RoundPhase.FAST_MONEY -> "الجولة السريعة"
    RoundPhase.GAME_OVER -> "انتهت اللعبة"
}
