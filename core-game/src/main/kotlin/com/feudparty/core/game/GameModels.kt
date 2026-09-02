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

/**
 * مراحل الجولة — نفس تسلسل البرنامج:
 * المواجهة ← اللعب ← (٣ أخطاء) السرقة ← نهاية الجولة.
 */
@Serializable
enum class RoundPhase {
    /** الزر مفتوح للفريقين، أول واحد بيبزّ بيجاوب. */
    FACE_OFF,

    /** الفريق التاني بياخد فرصته بالمواجهة (بدون بزّ). */
    FACE_OFF_SECOND,

    /** الفريق اللي فاز بالمواجهة بيلعب اللوح لحاله. */
    PLAY,

    /** الفريق المقابل عنده محاولة وحدة يسرق فيها كل نقاط الجولة. */
    STEAL,

    /** انتهت الجولة وانوزعت النقاط — بستنى «الجولة التالية». */
    ROUND_END,

    /** الجولة السريعة (Fast Money) للفريق الفايز. */
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

/** آخر توزيع نقاط — بينعرض كبانر على كل الأجهزة. */
@Serializable
data class Award(
    val teamId: TeamId,
    val points: Int,
    val stolen: Boolean = false
)

/** جواب واحد باللعبة السريعة. */
@Serializable
data class FastMoneyEntry(
    val answerIndex: Int?,
    val points: Int,
    val duplicate: Boolean = false
) {
    val passed: Boolean get() = answerIndex == null
}

/**
 * الجولة السريعة: لاعبين من الفريق الفايز، ٥ أسئلة لكل واحد.
 * الأول عنده [FIRST_PLAYER_SECONDS] ثانية والتاني [SECOND_PLAYER_SECONDS]،
 * والهدف [TARGET] نقطة مجموع.
 */
@Serializable
data class FastMoneyState(
    val questions: List<Question>,
    val teamId: TeamId,
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
    val total: Int get() = playerOne.sumOf { it.points } + playerTwo.sumOf { it.points }
    val won: Boolean get() = total >= TARGET
    val playerOneTotal: Int get() = playerOne.sumOf { it.points }
    val playerTwoTotal: Int get() = playerTwo.sumOf { it.points }

    /** الأجوبة اللي استعملها اللاعب الأول — التاني ما بينفع يكررها. */
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
    val phase: RoundPhase = RoundPhase.FACE_OFF,
    val buzzState: BuzzState = BuzzState.OPEN,
    /** نقاط الجولة المتجمّعة — بتروح كلها لفريق واحد بنهاية الجولة. */
    val pot: Int = 0,
    val strikes: Int = 0,
    val controllingTeam: TeamId? = null,
    /** مين دوره يجاوب حالياً بالمواجهة. */
    val faceOffTeam: TeamId? = null,
    /** صاحب أعلى جواب بالمواجهة لحد الآن. */
    val faceOffLeader: TeamId? = null,
    val faceOffLeaderPoints: Int = 0,
    val roundWinner: TeamId? = null,
    val lastAward: Award? = null,
    /** مضاعف نقاط كل جولة — الجولة رقم i بتاخد multipliers[i]. */
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

    /** الفريق اللي دوره يجاوب حالياً — لتلوين الواجهة عند الكل. */
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
}

fun GameState.buzzedTeam(): TeamId? = when (buzzState) {
    BuzzState.LOCKED_TEAM_1 -> TeamId.TEAM_1
    BuzzState.LOCKED_TEAM_2 -> TeamId.TEAM_2
    else -> null
}

/**
 * نسخة الحالة اللي بتنبعت لأجهزة الفرق: نصوص الأجوبة المخفية بتنشال حتى
 * ما يقدر حدا يقرأها من الشبكة قبل ما المضيف يكشفها.
 */
fun GameState.maskedForTeams(): GameState = copy(
    questions = questions.map { it.masked() },
    fastMoneyQuestions = emptyList(),
    fastMoney = fastMoney?.let { fm ->
        fm.copy(questions = if (fm.revealed) fm.questions else fm.questions.map { it.masked() })
    }
)

private fun Question.masked(): Question =
    copy(answers = answers.map { if (it.revealed) it else it.copy(text = "") })
