package com.feudparty.app.settings

import com.feudparty.core.game.FastMoneyState

/**
 * إعدادات المضيف — بتتحفظ على جهازه وبتتطبّق على كل لعبة جديدة.
 * [bankName] بيضل null إذا المضيف عم يستعمل البنك المرفق مع التطبيق.
 */
data class GameSettings(
    val rounds: Int = DEFAULT_ROUNDS,
    val multipliers: List<Int> = DEFAULT_MULTIPLIERS,
    val strikesToSteal: Int = DEFAULT_STRIKES,
    val fastMoneyEnabled: Boolean = true,
    val fastMoneyTarget: Int = FastMoneyState.TARGET,
    val fastMoneyFirstSeconds: Int = FastMoneyState.FIRST_PLAYER_SECONDS,
    val fastMoneySecondSeconds: Int = FastMoneyState.SECOND_PLAYER_SECONDS,
    val bankName: String? = null,
    val bankQuestionCount: Int = 0,
    /** وضع الاختبار: وصل عبر TCP بدل Nearby حتى نلعب على محاكيات. */
    val lanTesting: Boolean = false,
    val lanHost: String = DEFAULT_LAN_HOST
) {
    /** مضاعف كل جولة — إذا المضاعفات أقل من عدد الجولات منكرر الأخير. */
    fun multipliersForRounds(): List<Int> = List(rounds) { index ->
        multipliers.getOrElse(index) { multipliers.lastOrNull() ?: 1 }
    }

    /** عدد الأسئلة اللي لازمة للعبة وحدة. */
    fun questionsNeeded(): Int =
        rounds + if (fastMoneyEnabled) FastMoneyState.QUESTIONS_PER_PLAYER else 0

    fun clamped(): GameSettings = copy(
        rounds = rounds.coerceIn(MIN_ROUNDS, MAX_ROUNDS),
        multipliers = multipliers.map { it.coerceIn(1, 9) }.ifEmpty { DEFAULT_MULTIPLIERS },
        strikesToSteal = strikesToSteal.coerceIn(MIN_STRIKES, MAX_STRIKES),
        fastMoneyTarget = fastMoneyTarget.coerceIn(50, 500),
        fastMoneyFirstSeconds = fastMoneyFirstSeconds.coerceIn(10, 90),
        fastMoneySecondSeconds = fastMoneySecondSeconds.coerceIn(10, 90)
    )

    companion object {
        const val MIN_ROUNDS = 1
        const val MAX_ROUNDS = 8
        const val DEFAULT_ROUNDS = 4
        const val MIN_STRIKES = 1
        const val MAX_STRIKES = 5
        const val DEFAULT_STRIKES = 3
        val DEFAULT_MULTIPLIERS = listOf(1, 1, 2, 3)
        const val DEFAULT_LAN_HOST = "127.0.0.1"
    }
}
