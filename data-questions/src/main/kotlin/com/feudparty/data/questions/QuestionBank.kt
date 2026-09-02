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
    val id: String,
    val text: String,
    val category: String,
    val answers: List<RawAnswer>
)

/** أسئلة لعبة وحدة: جولات عادية + أسئلة الجولة السريعة. */
data class GameQuestions(
    val rounds: List<Question>,
    val fastMoney: List<Question>
)

/** بنك الأسئلة — بيتقرأ مرة وحدة من ملف JSON مرفق مع التطبيق. */
object QuestionBank {
    private const val RESOURCE = "starter_questions.json"
    private val json = Json { ignoreUnknownKeys = true }

    private val questions: List<Question> by lazy { parse() }

    fun load(): List<Question> = questions

    /**
     * بيختار [count] سؤال عشوائي لجولة وحدة — بدون تكرار، ومو أكتر من
     * عدد الأسئلة الموجودة بالبنك.
     */
    fun randomRound(count: Int, random: Random = Random.Default): List<Question> =
        questions.shuffled(random).take(count.coerceAtMost(questions.size))

    /**
     * توزيعة لعبة كاملة: أسئلة الجولات العادية + أسئلة الجولة السريعة،
     * كلها من نفس السحبة فما بينكرر سؤال بين القسمين.
     */
    fun randomGame(
        rounds: Int,
        fastMoneyCount: Int = FastMoneyState.QUESTIONS_PER_PLAYER,
        random: Random = Random.Default
    ): GameQuestions {
        val shuffled = questions.shuffled(random)
        val roundQuestions = shuffled.take(rounds)
        val fastMoney = shuffled.drop(roundQuestions.size).take(fastMoneyCount)
        return GameQuestions(rounds = roundQuestions, fastMoney = fastMoney)
    }

    private fun parse(): List<Question> {
        val stream = QuestionBank::class.java.classLoader
            ?.getResourceAsStream(RESOURCE)
            ?: error("$RESOURCE not found on classpath")
        val raw = stream.use {
            json.decodeFromString<List<RawQuestion>>(it.readBytes().decodeToString())
        }
        return raw.map { rq ->
            Question(
                id = rq.id,
                text = rq.text,
                category = rq.category,
                answers = rq.answers
                    .sortedByDescending { it.points }
                    .map { Answer(text = it.text, points = it.points) }
            )
        }
    }
}
