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
    val category: String,
    /** انقرأ قبل هيك؟ الأسئلة المقروءة ما بترجع إلا لما يخلصوا كلهن. */
    val isRead: Boolean = false
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
 * لاعب واحد = جهاز واحد.
 *
 * [seat] هو رقمه بالفريق (١، ٢، ٣...) وبياخدو لما ينضم وبيضل إله. الرقم
 * هو أساس المواجهة: صاحب الرقم ١ بفريق بيواجه صاحب الرقم ١ بالفريق
 * التاني، والرقم ٢ مع الرقم ٢، وهكذا.
 */
@Serializable
data class Player(
    val id: String,
    val name: String,
    val teamId: TeamId,
    val seat: Int,
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

    /**
     * الفريق اللي فاز بالمواجهة بيختار: يلعب اللوح أو يمرّرو للفريق
     * التاني. الاختيار بيصير من جهاز لاعب المنصة نفسه.
     */
    PLAY_OR_PASS,

    /** الفريق اللي معه اللوح بيلعب، لاعب ورا لاعب بالدور. */
    PLAY,

    /** محاولة وحدة للفريق المقابل يسرق فيها نقاط الجولة. */
    STEAL,

    ROUND_END,

    /** نتيجة الفريقين بتظهر عند الكل قبل ما تبلّش الجولة الجاية. */
    SCOREBOARD,

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
    /** الفريق اللي كسب المواجهة وبيختار يلعب أو يمرّر. */
    val faceOffWinner: TeamId? = null,
    /** اللاعب اللي ضاغط حالياً (أزرق عند الكل). */
    val buzzedPlayerId: String? = null,
    /** اللاعب اللي دوره يجاوب بمرحلة اللعب أو السرقة. */
    val turnPlayerId: String? = null,
    /** الرقم اللي عليه الدور بالمواجهة — بيزيد كل جولة. */
    val faceOffSeat: Int = 1,
    /** مؤشر الدور داخل الفريق بمرحلة اللعب. */
    val turnIndex: Map<TeamId, Int> = emptyMap(),
    /** لاعبين جاوبوا غلط — بيضلوا حمر لحد ما يرجع دورهم. */
    val wrongPlayers: Set<String> = emptySet(),
    /** لاعبين جاوبوا صح — بيضلوا خضر لحد ما يرجع دورهم. */
    val correctPlayers: Set<String> = emptySet(),
    val roundWinner: TeamId? = null,
    val lastAward: Award? = null,
    val multipliers: List<Int> = listOf(1, 1, 2, 3),
    /** عدد الأخطاء اللي بتفتح السرقة — ٣ زي البرنامج. */
    val strikesToSteal: Int = 3,
    /** كم ثانية للاعب يجاوب قبل ما ينحسب عليه خطأ. */
    val answerLimitSeconds: Int = DEFAULT_ANSWER_SECONDS,
    /** كم ثانية للفائز بالمواجهة ليقرّر: يلعب أو يمرّر. */
    val choiceLimitSeconds: Int = CHOICE_SECONDS,
    /** الوقت الباقي للجواب — صفر يعني ما في عدّاد شغّال. */
    val answerSecondsLeft: Int = 0,
    /** الوقت الباقي لقرار «نلعب أو نمرّر». */
    val choiceSecondsLeft: Int = 0,
    /** صارت اللعبة تمشي — قبلها اللاعب بيقدر يبدّل فريقه. */
    val matchStarted: Boolean = false,
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
            RoundPhase.PLAY_OR_PASS -> faceOffWinner
            RoundPhase.PLAY -> controllingTeam
            RoundPhase.STEAL -> stealingTeam
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

    /** أكبر رقم موجود بالفريقين — عليه بتلف المواجهة. */
    val maxSeat: Int
        get() = players.maxOfOrNull { it.seat } ?: 1

    /**
     * لاعب المنصة للفريق: صاحب الرقم [faceOffSeat]. إذا الفريق أقصر من
     * الرقم، منلفّ عليه من الأول حتى يضل في مواجهة.
     */
    fun podiumPlayer(teamId: TeamId): Player? {
        val list = playersOf(teamId)
        if (list.isEmpty()) return null
        return list.firstOrNull { it.seat == faceOffSeat }
            ?: list[(faceOffSeat - 1) % list.size]
    }

    /** اللاعب اللي قدّامه بالفريق التاني — نفس الرقم. */
    fun opponentOf(player: Player): Player? =
        playersOf(player.teamId.other()).firstOrNull { it.seat == player.seat }

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
        RoundPhase.PLAY_OR_PASS -> setOfNotNull(faceOffWinner?.let { podiumPlayer(it)?.id })
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

/** ثواني قرار «نلعب أو نمرّر». */
const val CHOICE_SECONDS = 5

/** الوقت الافتراضي للجواب. */
const val DEFAULT_ANSWER_SECONDS = 10

fun GameState.buzzedTeam(): TeamId? = when (buzzState) {
    BuzzState.LOCKED_TEAM_1 -> TeamId.TEAM_1
    BuzzState.LOCKED_TEAM_2 -> TeamId.TEAM_2
    else -> null
}

/**
 * نسخة الحالة اللي بتنبعت لأجهزة اللاعبين: بدون نص السؤال وبدون نصوص
 * الأجوبة المخفية. اللاعب بيسمع السؤال من المضيف، وبيشوف خانات مرقّمة بس.
 */
fun GameState.maskedForPlayers(): GameState {
    return copy(
        // نص السؤال ما بيوصل ولا جهاز لاعب — بيسمعوه من المضيف بس.
        questions = questions.map { it.masked(hideText = true) },
    )
}

private fun Question.masked(hideText: Boolean): Question = copy(
    text = if (hideText) "" else text,
    // الأجوبة المخفية ما بتنبعت أبداً — اللوح بيعرض خانات فاضية.
    answers = answers.map { if (it.revealed) it else it.copy(text = "") }
)
