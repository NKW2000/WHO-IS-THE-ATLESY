package com.feudparty.app.settings


/**
 * إعدادات المضيف — بتتحفظ على جهازه وبتتطبّق على كل لعبة جديدة.
 * [bankName] بيضل null إذا المضيف عم يستعمل البنك المرفق مع التطبيق.
 */
data class GameSettings(
    val rounds: Int = DEFAULT_ROUNDS,
    val multipliers: List<Int> = DEFAULT_MULTIPLIERS,
    val strikesToSteal: Int = DEFAULT_STRIKES,
    val bankName: String? = null,
    val bankQuestionCount: Int = 0
) {
    /** مضاعف كل جولة — إذا المضاعفات أقل من عدد الجولات منكرر الأخير. */
    fun multipliersForRounds(): List<Int> = List(rounds) { index ->
        multipliers.getOrElse(index) { multipliers.lastOrNull() ?: 1 }
    }

    /** عدد الأسئلة اللي لازمة للعبة وحدة — سؤال لكل جولة. */
    fun questionsNeeded(): Int = rounds

    fun clamped(): GameSettings = copy(
        rounds = rounds.coerceIn(MIN_ROUNDS, MAX_ROUNDS),
        multipliers = multipliers.map { it.coerceIn(1, 9) }.ifEmpty { DEFAULT_MULTIPLIERS },
        strikesToSteal = strikesToSteal.coerceIn(MIN_STRIKES, MAX_STRIKES)
    )

    companion object {
        const val MIN_ROUNDS = 1
        const val MAX_ROUNDS = 8
        const val DEFAULT_ROUNDS = 4
        const val MIN_STRIKES = 1
        const val MAX_STRIKES = 5
        const val DEFAULT_STRIKES = 3
        val DEFAULT_MULTIPLIERS = listOf(1, 1, 2, 3)
    }
}
