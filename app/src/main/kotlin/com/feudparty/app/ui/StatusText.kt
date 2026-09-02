package com.feudparty.app.ui

import com.feudparty.core.game.GameState
import com.feudparty.core.game.RoundPhase
import com.feudparty.core.game.TeamId
import com.feudparty.core.game.buzzedTeam

private fun GameState.nameOf(teamId: TeamId?): String =
    teamId?.let { teams[it]?.name } ?: "الفريق"

/** نص الحالة عند المضيف — دايماً بيقول له شو المطلوب منه هلق. */
fun hostStatusText(state: GameState): String = when (state.phase) {
    RoundPhase.FACE_OFF -> when (val buzzed = state.buzzedTeam()) {
        null -> "المواجهة — الزر مفتوح للفريقين"
        else -> "${state.nameOf(buzzed)} بزّ أول — احكم على جوابه"
    }

    RoundPhase.FACE_OFF_SECOND ->
        "دور ${state.nameOf(state.faceOffTeam)} بالمواجهة — لازم جواب أعلى"

    RoundPhase.PLAY ->
        "${state.nameOf(state.controllingTeam)} ماسك اللوح — ${state.strikes}/3 أخطاء"

    RoundPhase.STEAL ->
        "فرصة سرقة لـ ${state.nameOf(state.stealingTeam)} — جواب واحد بس"

    RoundPhase.ROUND_END -> "انتهت الجولة — انتقل للجولة الجاية"
    RoundPhase.FAST_MONEY -> "الجولة السريعة"
    RoundPhase.GAME_OVER -> "انتهت اللعبة"
}

/** نص الحالة عند جهاز الفريق — من وجهة نظر [myTeam]. */
fun teamStatusText(state: GameState?, myTeam: TeamId?): String {
    if (state == null) return "بانتظار المضيف"
    val mine = myTeam != null && state.activeTeam == myTeam

    return when (state.phase) {
        RoundPhase.FACE_OFF -> when (val buzzed = state.buzzedTeam()) {
            null -> "المواجهة — اضغط الزر أول واحد!"
            myTeam -> "بزّيت أول — جاوب!"
            else -> "${state.nameOf(buzzed)} بزّ أول"
        }

        RoundPhase.FACE_OFF_SECOND ->
            if (mine) "دورك — جواب أعلى بياخد اللوح" else "دور الفريق التاني بالمواجهة"

        RoundPhase.PLAY ->
            if (mine) "اللوح إلكم — كملوا الأجوبة" else "${state.nameOf(state.controllingTeam)} بيلعب اللوح"

        RoundPhase.STEAL ->
            if (mine) "فرصة سرقة! جواب واحد بياخد كل النقاط" else "الفريق التاني بيحاول يسرق"

        RoundPhase.ROUND_END -> {
            val winner = state.roundWinner
            when {
                winner == null -> "انتهت الجولة"
                winner == myTeam -> "الجولة إلكم! +${state.lastAward?.points ?: 0}"
                else -> "الجولة راحت لـ ${state.nameOf(winner)}"
            }
        }

        RoundPhase.FAST_MONEY ->
            if (state.fastMoney?.teamId == myTeam) "الجولة السريعة — دوركم!" else "الجولة السريعة للفريق التاني"

        RoundPhase.GAME_OVER -> "انتهت اللعبة"
    }
}
