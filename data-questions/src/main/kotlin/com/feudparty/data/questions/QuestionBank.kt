package com.feudparty.data.questions

import com.feudparty.core.game.Answer
import com.feudparty.core.game.FastMoneyState
import com.feudparty.core.game.Question
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Serializable
private data class RawAnswer(val text: String, val points: Int)

@Serializable
private data class RawQuestion(
    val id: String? = null,
    val text: String,
    val category: String = "عام",
    val answers: List<RawAnswer>
)

/** أسئلة لعبة وحدة: جولات عادية + أسئلة الجولة السريعة. */
data class GameQuestions(
    val rounds: List<Question>,
    val fastMoney: List<Question>
)

/** نتيجة قراءة ملف بنك أسئلة من المضيف. */
sealed class BankResult {
    data class Success(val questions: List<Question>) : BankResult()

    /** [message] عربي وجاهز للعرض للمضيف. */
    data class Failure(val message: String) : BankResult()
}

/**
 * بنك الأسئلة. في بنك مرفق مع التطبيق، والمضيف بيقدر يستورد بنكه الخاص
 * من ملف JSON بنفس الشكل.
 */
object QuestionBank {
    private const val RESOURCE = "starter_questions.json"
    private val json = Json { ignoreUnknownKeys = true }

    /** أقل وأكثر عدد أجوبة مسموح فيه بالسؤال الواحد. */
    const val MIN_ANSWERS = 2
    const val MAX_ANSWERS = 8

    private val bundled: List<Question> by lazy {
        when (val result = parse(readResource())) {
            is BankResult.Success -> result.questions
            is BankResult.Failure -> error("bundled bank invalid: ${result.message}")
        }
    }

    fun load(): List<Question> = bundled

    /**
     * بيقرأ ملف بنك أسئلة ويتأكد منه. الشكل المتوقع:
     *
     * ```json
     * [
     *   {
     *     "text": "اذكر شي بيعمله الناس أول ما يصحوا",
     *     "category": "عام",
     *     "answers": [
     *       {"text": "يشيّكوا الموبايل", "points": 40},
     *       {"text": "يشربوا قهوة", "points": 30}
     *     ]
     *   }
     * ]
     * ```
     *
     * `id` و`category` اختياريين. الأجوبة بتنرتب من الأعلى نقاط للأقل،
     * لأن ترتيبها هو ترتيب اللوح وجواب رقم ١ بياخد اللوح بالمواجهة.
     */
    fun parse(text: String): BankResult {
        val raw = try {
            json.decodeFromString<List<RawQuestion>>(text)
        } catch (error: Exception) {
            return BankResult.Failure("الملف مش JSON صالح: ${error.message ?: "خطأ بالقراءة"}")
        }

        if (raw.isEmpty()) return BankResult.Failure("الملف فاضي — ما في ولا سؤال")

        val questions = mutableListOf<Question>()
        raw.forEachIndexed { index, rawQuestion ->
            val position = index + 1
            if (rawQuestion.text.isBlank()) {
                return BankResult.Failure("السؤال رقم $position بدون نص")
            }
            if (rawQuestion.answers.size < MIN_ANSWERS) {
                return BankResult.Failure(
                    "السؤال رقم $position لازم يكون فيه $MIN_ANSWERS أجوبة عالأقل"
                )
            }
            if (rawQuestion.answers.size > MAX_ANSWERS) {
                return BankResult.Failure(
                    "السؤال رقم $position فيه أجوبة أكتر من $MAX_ANSWERS"
                )
            }
            rawQuestion.answers.forEach { answer ->
                if (answer.text.isBlank()) {
                    return BankResult.Failure("بالسؤال رقم $position في جواب بدون نص")
                }
                if (answer.points <= 0) {
                    return BankResult.Failure(
                        "بالسؤال رقم $position في جواب نقاطه صفر أو أقل"
                    )
                }
            }

            questions += Question(
                id = rawQuestion.id?.takeIf { it.isNotBlank() } ?: "q$position",
                text = rawQuestion.text.trim(),
                category = rawQuestion.category.trim().ifBlank { "عام" },
                // ترتيب اللوح دايماً من الأعلى نقاط للأقل.
                answers = rawQuestion.answers
                    .sortedByDescending { it.points }
                    .map { Answer(text = it.text.trim(), points = it.points) }
            )
        }
        return BankResult.Success(questions)
    }

    /**
     * بيختار [count] سؤال عشوائي لجولة وحدة — بدون تكرار، ومو أكتر من
     * عدد الأسئلة الموجودة بالبنك.
     */
    fun randomRound(
        count: Int,
        source: List<Question> = bundled,
        random: Random = Random.Default
    ): List<Question> = source.shuffled(random).take(count.coerceAtMost(source.size))

    /**
     * توزيعة لعبة كاملة: أسئلة الجولات العادية + أسئلة الجولة السريعة،
     * كلها من نفس السحبة فما بينكرر سؤال بين القسمين.
     */
    fun randomGame(
        rounds: Int,
        fastMoneyCount: Int = FastMoneyState.QUESTIONS_PER_PLAYER,
        source: List<Question> = bundled,
        random: Random = Random.Default
    ): GameQuestions {
        val shuffled = source.shuffled(random)
        val roundQuestions = shuffled.take(rounds)
        val fastMoney = shuffled.drop(roundQuestions.size).take(fastMoneyCount)
        return GameQuestions(rounds = roundQuestions, fastMoney = fastMoney)
    }

    private fun readResource(): String {
        val stream = QuestionBank::class.java.classLoader
            ?.getResourceAsStream(RESOURCE)
            ?: error("$RESOURCE not found on classpath")
        return stream.use { it.readBytes().decodeToString() }
    }
}
