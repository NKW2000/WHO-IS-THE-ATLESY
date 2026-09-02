package com.feudparty.core.game

sealed class GameEvent {
    /** بزّة من جهاز فريق — بتنقبل بس بمرحلة المواجهة والزر مفتوح. */
    data class Buzz(val teamId: TeamId, val atMillis: Long) : GameEvent()

    /** المضيف حكم إنه الجواب صح وكشف الخانة رقم [answerIndex]. */
    data class JudgeCorrect(val answerIndex: Int) : GameEvent()

    /** جواب غلط — بالمواجهة بينقل الدور، وباللعب بيزيد خطأ (X). */
    object JudgeWrong : GameEvent()

    /** الانتقال للجولة التالية (أو للجولة السريعة أو نهاية اللعبة). */
    object NextRound : GameEvent()

    data class TeamJoined(val teamId: TeamId, val teamName: String) : GameEvent()
    data class TeamLeft(val teamId: TeamId) : GameEvent()

    /** الجولة السريعة. */
    object FastMoneyStartTimer : GameEvent()
    object FastMoneyTick : GameEvent()

    /** المضيف بيسجّل جواب اللاعب: [answerIndex] = null يعني ما جاوب. */
    data class FastMoneySubmit(val answerIndex: Int?) : GameEvent()

    /** كشف كل أجوبة الجولة السريعة بعد ما يخلص اللاعبين. */
    object FastMoneyReveal : GameEvent()

    object EndGame : GameEvent()
}
