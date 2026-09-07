package com.feudparty.app.settings

import com.feudparty.core.game.TeamId


/**
 * إعدادات المضيف — بتتحفظ على جهازه وبتتطبّق على كل لعبة جديدة.
 * [bankName] بيضل null إذا المضيف عم يستعمل البنك المرفق مع التطبيق.
 */
data class GameSettings(
    val rounds: Int = DEFAULT_ROUNDS,
    val multipliers: List<Int> = DEFAULT_MULTIPLIERS,
    val strikesToSteal: Int = DEFAULT_STRIKES,
    /** كم ثانية عند اللاعب ليجاوب قبل ما ينحسب عليه خطأ. */
    val answerSeconds: Int = DEFAULT_ANSWER_SECONDS,
    val teamNames: Map<TeamId, String> = DEFAULT_TEAM_NAMES,
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
        strikesToSteal = strikesToSteal.coerceIn(MIN_STRIKES, MAX_STRIKES),
        answerSeconds = answerSeconds.coerceIn(MIN_ANSWER_SECONDS, MAX_ANSWER_SECONDS),
        teamNames = TeamId.entries.associateWith { id ->
            teamNames[id]?.trim()?.take(MAX_TEAM_NAME)?.ifBlank { null }
                ?: DEFAULT_TEAM_NAMES.getValue(id)
        }
    )

    fun teamName(teamId: TeamId): String =
        teamNames[teamId] ?: DEFAULT_TEAM_NAMES.getValue(teamId)

    companion object {
        const val MIN_ROUNDS = 1
        const val MAX_ROUNDS = 8
        const val DEFAULT_ROUNDS = 4
        const val MIN_STRIKES = 1
        const val MAX_STRIKES = 5
        const val DEFAULT_STRIKES = 3
        val DEFAULT_MULTIPLIERS = listOf(1, 1, 2, 3)
        const val MIN_ANSWER_SECONDS = 5
        const val MAX_ANSWER_SECONDS = 60
        const val DEFAULT_ANSWER_SECONDS = 10
        const val MAX_TEAM_NAME = 18
        val DEFAULT_TEAM_NAMES = mapOf(
            TeamId.TEAM_1 to "الفريق الأخضر",
            TeamId.TEAM_2 to "الفريق الأزرق"
        )
    }
}
