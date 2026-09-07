package com.feudparty.core.game

sealed class GameEvent {
    /** بزّة من جهاز لاعب — بتنقبل بس إذا هو المسموح له يضغط هلق. */
    data class Buzz(val playerId: String, val atMillis: Long) : GameEvent()

    /** المضيف حكم إنه الجواب صح وكشف الخانة رقم [answerIndex]. */
    data class JudgeCorrect(val answerIndex: Int) : GameEvent()

    /** جواب غلط — بالمواجهة بينقل الدور، وباللعب بيزيد خطأ (X). */
    object JudgeWrong : GameEvent()

    /**
     * قرار الفريق اللي كسب المواجهة: يلعب اللوح ([play] = true) أو
     * يمرّرو للفريق التاني.
     */
    data class ChooseControl(val play: Boolean) : GameEvent()

    /** الانتقال للجولة التالية (أو للجولة السريعة أو نهاية اللعبة). */
    object NextRound : GameEvent()

    data class PlayerJoined(
        val playerId: String,
        val name: String,
        val teamId: TeamId
    ) : GameEvent()

    data class PlayerLeft(val playerId: String) : GameEvent()

    /** المضيف أو اللاعب نفسه بيغيّر فريقه قبل ما تبلّش اللعبة. */
    data class PlayerMoved(val playerId: String, val teamId: TeamId) : GameEvent()

    /** ثانية مرقت — بتنقص من وقت الجواب أو وقت القرار. */
    object Tick : GameEvent()

    object EndGame : GameEvent()
}
