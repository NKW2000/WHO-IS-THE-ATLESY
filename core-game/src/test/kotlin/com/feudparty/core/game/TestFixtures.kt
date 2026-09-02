package com.feudparty.core.game

/** لوح من ٤ أجوبة — نفس السؤال بكل الاختبارات حتى تضل الأرقام متوقعة. */
internal fun board(id: String = "q1"): Question = Question(
    id = id,
    text = "سؤال $id",
    category = "عام",
    answers = listOf(
        Answer("الأول", 40),
        Answer("التاني", 30),
        Answer("التالت", 20),
        Answer("الرابع", 10)
    )
)

internal fun freshState(
    questions: List<Question> = listOf(board("q1"), board("q2")),
    multipliers: List<Int> = listOf(1, 2),
    fastMoneyQuestions: List<Question> = emptyList()
) = GameState(
    questions = questions,
    multipliers = multipliers,
    fastMoneyQuestions = fastMoneyQuestions,
    teams = mapOf(
        TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١", connected = true),
        TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢", connected = true)
    )
)

internal fun GameEngine.buzz(teamId: TeamId) = apply(GameEvent.Buzz(teamId, atMillis = 0))

internal fun GameEngine.correct(index: Int) = apply(GameEvent.JudgeCorrect(index))

internal fun GameEngine.wrong() = apply(GameEvent.JudgeWrong)

/** بتوصل اللعبة لمرحلة اللعب مع [team] ماسك اللوح وجواب رقم ١ مكشوف. */
internal fun GameEngine.giveControlTo(team: TeamId): GameState {
    buzz(team)
    return correct(0)
}

internal fun GameState.score(team: TeamId): Int = teams.getValue(team).score
