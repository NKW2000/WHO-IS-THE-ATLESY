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

/** ٣ لاعبين لكل فريق: a1,a2,a3 و b1,b2,b3. */
internal fun defaultPlayers(perTeam: Int = 3): List<Player> =
    (1..perTeam).map { Player("a$it", "لاعب أ$it", TeamId.TEAM_1) } +
        (1..perTeam).map { Player("b$it", "لاعب ب$it", TeamId.TEAM_2) }

internal fun freshState(
    questions: List<Question> = listOf(board("q1"), board("q2")),
    multipliers: List<Int> = listOf(1, 2),
    players: List<Player> = defaultPlayers()
) = GameState(
    questions = questions,
    multipliers = multipliers,
    players = players,
    teams = mapOf(
        TeamId.TEAM_1 to TeamState(TeamId.TEAM_1, "فريق ١", connected = true),
        TeamId.TEAM_2 to TeamState(TeamId.TEAM_2, "فريق ٢", connected = true)
    )
)

internal fun GameEngine.buzz(playerId: String) = apply(GameEvent.Buzz(playerId, atMillis = 0))

/** بيضغط لاعب المنصة الحالي لهاد الفريق. */
internal fun GameEngine.buzzPodium(team: TeamId): GameState =
    buzz(state.podiumPlayer(team)!!.id)

internal fun GameEngine.correct(index: Int) = apply(GameEvent.JudgeCorrect(index))

internal fun GameEngine.wrong() = apply(GameEvent.JudgeWrong)

/** بتوصل اللعبة لمرحلة اللعب مع [team] ماسك اللوح وجواب رقم ١ مكشوف. */
internal fun GameEngine.giveControlTo(team: TeamId): GameState {
    buzzPodium(team)
    return correct(0)
}

internal fun GameState.score(team: TeamId): Int = teams.getValue(team).score
