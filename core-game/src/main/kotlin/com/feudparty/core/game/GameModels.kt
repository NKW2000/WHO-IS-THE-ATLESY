package com.feudparty.core.game

import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    val text: String,
    val points: Int,
    val revealed: Boolean = false
)

@Serializable
data class Question(
    val id: String,
    val text: String,
    val answers: List<Answer>,
    val category: String
)

@Serializable
enum class TeamId { TEAM_1, TEAM_2 }

fun TeamId.other(): TeamId = when (this) {
    TeamId.TEAM_1 -> TeamId.TEAM_2
    TeamId.TEAM_2 -> TeamId.TEAM_1
}

@Serializable
data class TeamState(
    val id: TeamId,
    val name: String,
    val score: Int = 0,
    val connected: Boolean = false
)

/** لاعب واحد = جهاز واحد. الترتيب باللستة هو ترتيب الدور بالفريق. */
@Serializable
data class Player(
    val id: String,
    val name: String,
    val teamId: TeamId,
    val connected: Boolean = true
)

/**
 * حالة شاشة اللاعب — منها بيتحدد لون الجهاز كله:
 * وميض أبيض/أسود لما يكون دوره، أزرق لما يضغط، أخضر إذا صحّ، أحمر إذا غلط
 * (وبيضل أحمر لحد ما يرجع دوره).
 */
@Serializable
enum class PlayerMark { IDLE, ARMED, BUZZED, CORRECT, WRONG }

@Serializable
enum class RoundPhase {
    /** الزر مفتوح للاعبَي المنصة (واحد من كل فريق). */
    FACE_OFF,

    /** لاعب المنصة من الفريق التاني بياخد فرصته. */
    FACE_OFF_SECOND,

    /** الفريق اللي فاز بالمواجهة بيلعب اللوح، لاعب ورا لاعب بالدور. */
    PLAY,

    /** محاولة وحدة للفريق المقابل يسرق فيها نقاط الجولة. */
    STEAL,

    ROUND_END,
    FAST_MONEY,
    GAME_OVER
}

@Serializable
enum class BuzzState {
    OPEN,
    LOCKED_TEAM_1,
    LOCKED_TEAM_2,
    CLOSED
}

@Serializable
data class Award(
    val teamId: TeamId,
    val points: Int,
    val stolen: Boolean = false
)

@Serializable
data class FastMoneyEntry(
    val answerIndex: Int?,
    val points: Int,
    val duplicate: Boolean = false
) {
    val passed: Boolean get() = answerIndex == null
}

@Serializable
data class FastMoneyState(
    val questions: List<Question>,
    val teamId: TeamId,
    val playerIds: List<String> = emptyList(),
    val playerIndex: Int = 0,
    val questionIndex: Int = 0,
    val playerOne: List<FastMoneyEntry> = emptyList(),
    val playerTwo: List<FastMoneyEntry> = emptyList(),
    val secondsRemaining: Int = FIRST_PLAYER_SECONDS,
    val timerRunning: Boolean = false,
    val duplicateFlag: Boolean = false,
    val revealed: Boolean = false,
    val finished: Boolean = false
) {
    val currentQuestion: Question? get() = questions.getOrNull(questionIndex)
    val currentEntries: List<FastMoneyEntry> get() = if (playerIndex == 0) playerOne else playerTwo
    val currentPlayerId: String? get() = playerIds.getOrNull(playerIndex)
    val total: Int get() = playerOne.sumOf { it.points } + playerTwo.sumOf { it.points }
    val won: Boolean get() = total >= TARGET
    val playerOneTotal: Int get() = playerOne.sumOf { it.points }
    val playerTwoTotal: Int get() = playerTwo.sumOf { it.points }
    val usedByPlayerOne: Set<Int> get() = playerOne.mapNotNull { it.answerIndex }.toSet()

    companion object {
        const val TARGET = 200
        const val FIRST_PLAYER_SECONDS = 20
        const val SECOND_PLAYER_SECONDS = 25
        const val QUESTIONS_PER_PLAYER = 5
    }
}

@Serializable
data class GameState(
    val questions: List<Question>,
    val currentQuestionIndex: Int = 0,
    val teams: Map<TeamId, TeamState>,
    val players: List<Player> = emptyList(),
    val phase: RoundPhase = RoundPhase.FACE_OFF,
    val buzzState: BuzzState = BuzzState.OPEN,
    val pot: Int = 0,
    val strikes: Int = 0,
    val controllingTeam: TeamId? = null,
    val faceOffTeam: TeamId? = null,
    val faceOffLeader: TeamId? = null,
    val faceOffLeaderPoints: Int = 0,
    /** اللاعب اللي ضاغط حالياً (أزرق عند الكل). */
    val buzzedPlayerId: String? = null,
    /** اللاعب اللي دوره يجاوب بمرحلة اللعب أو السرقة. */
    val turnPlayerId: String? = null,
    /** لاعب المنصة الحالي لكل فريق — بيتغير كل جولة. */
    val podiumIndex: Map<TeamId, Int> = emptyMap(),
    /** مؤشر الدور داخل الفريق بمرحلة اللعب. */
    val turnIndex: Map<TeamId, Int> = emptyMap(),
    /** لاعبين جاوبوا غلط — بيضلوا حمر لحد ما يرجع دورهم. */
    val wrongPlayers: Set<String> = emptySet(),
    /** لاعبين جاوبوا صح — بيضلوا خضر لحد ما يرجع دورهم. */
    val correctPlayers: Set<String> = emptySet(),
    val roundWinner: TeamId? = null,
    val lastAward: Award? = null,
    val multipliers: List<Int> = listOf(1, 1, 2, 3),
    val fastMoneyQuestions: List<Question> = emptyList(),
    val fastMoney: FastMoneyState? = null,
    val gameOver: Boolean = false
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)

    val multiplier: Int
        get() = multipliers.getOrElse(currentQuestionIndex) { multipliers.lastOrNull() ?: 1 }

    val roundOver: Boolean get() = phase == RoundPhase.ROUND_END

    val isLastRound: Boolean get() = currentQuestionIndex >= questions.lastIndex

    val stealingTeam: TeamId?
        get() = if (phase == RoundPhase.STEAL) controllingTeam?.other() else null

    val activeTeam: TeamId?
        get() = when (phase) {
            RoundPhase.FACE_OFF, RoundPhase.FACE_OFF_SECOND -> faceOffTeam
            RoundPhase.PLAY -> controllingTeam
            RoundPhase.STEAL -> stealingTeam
            RoundPhase.FAST_MONEY -> fastMoney?.teamId
            else -> null
        }

    val leadingTeam: TeamId?
        get() {
            val one = teams[TeamId.TEAM_1]?.score ?: 0
            val two = teams[TeamId.TEAM_2]?.score ?: 0
            return when {
                one > two -> TeamId.TEAM_1
                two > one -> TeamId.TEAM_2
                else -> null
            }
        }

    fun playersOf(teamId: TeamId): List<Player> = players.filter { it.teamId == teamId }

    fun player(playerId: String?): Player? = players.firstOrNull { it.id == playerId }

    /** لاعب المنصة الحالي للفريق — هو الوحيد اللي بيبزّ بالمواجهة. */
    fun podiumPlayer(teamId: TeamId): Player? {
        val list = playersOf(teamId)
        if (list.isEmpty()) return null
        return list[(podiumIndex[teamId] ?: 0) % list.size]
    }

    /** مين مسموح له يضغط هلق — عليهم بيومض الزر. */
    fun armedPlayerIds(): Set<String> = when (phase) {
        RoundPhase.FACE_OFF -> when (buzzState) {
            BuzzState.OPEN -> setOfNotNull(
                podiumPlayer(TeamId.TEAM_1)?.id,
                podiumPlayer(TeamId.TEAM_2)?.id
            )

            else -> emptySet()
        }

        RoundPhase.FACE_OFF_SECOND -> setOfNotNull(faceOffTeam?.let { podiumPlayer(it)?.id })
        RoundPhase.PLAY, RoundPhase.STEAL -> setOfNotNull(turnPlayerId)
        else -> emptySet()
    }

    /** حالة شاشة لاعب معيّن. */
    fun markFor(playerId: String?): PlayerMark = when {
        playerId == null -> PlayerMark.IDLE
        playerId == buzzedPlayerId -> PlayerMark.BUZZED
        playerId in wrongPlayers -> PlayerMark.WRONG
        playerId in correctPlayers -> PlayerMark.CORRECT
        playerId in armedPlayerIds() -> PlayerMark.ARMED
        else -> PlayerMark.IDLE
    }
}

fun GameState.buzzedTeam(): TeamId? = when (buzzState) {
    BuzzState.LOCKED_TEAM_1 -> TeamId.TEAM_1
    BuzzState.LOCKED_TEAM_2 -> TeamId.TEAM_2
    else -> null
}

/**
 * نسخة الحالة اللي بتنبعت لأجهزة اللاعبين: لا نص السؤال ولا نصوص الأجوبة
 * المخفية بتطلع من جهاز المضيف — اللاعب بيسمع السؤال من المضيف بس.
 */
fun GameState.maskedForPlayers(): GameState = copy(
    questions = questions.map { it.masked() },
    fastMoneyQuestions = emptyList(),
    fastMoney = fastMoney?.let { fm ->
        fm.copy(questions = if (fm.revealed) fm.questions else fm.questions.map { it.masked() })
    }
)

private fun Question.masked(): Question = copy(
    text = "",
    answers = answers.map { if (it.revealed) it else it.copy(text = "") }
)
