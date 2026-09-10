package com.feudparty.app.settings

import android.content.Context
import android.net.Uri
import com.feudparty.core.game.GameState
import com.feudparty.core.game.Question
import com.feudparty.core.game.TeamState
import com.feudparty.core.game.TeamId
import com.feudparty.data.questions.BankResult
import com.feudparty.data.questions.QuestionBank
import java.io.File

/**
 * بيحفظ إعدادات المضيف وبنك أسئلته على الجهاز. البنك المستورد بينسخ جوا
 * ملفات التطبيق حتى يضل شغّال حتى لو المستخدم مسح الملف الأصلي.
 */
class SettingsRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("host_settings", Context.MODE_PRIVATE)
    private val bankFile: File get() = File(appContext.filesDir, BANK_FILE)

    fun load(): GameSettings = GameSettings(
        rounds = prefs.getInt(KEY_ROUNDS, GameSettings.DEFAULT_ROUNDS),
        multipliers = prefs.getString(KEY_MULTIPLIERS, null)
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.takeIf { it.isNotEmpty() }
            ?: GameSettings.DEFAULT_MULTIPLIERS,
        strikesToSteal = prefs.getInt(KEY_STRIKES, GameSettings.DEFAULT_STRIKES),
        answerSeconds = prefs.getInt(KEY_ANSWER_SECONDS, GameSettings.DEFAULT_ANSWER_SECONDS),
        choiceSeconds = prefs.getInt(KEY_CHOICE_SECONDS, GameSettings.DEFAULT_CHOICE_SECONDS),
        roomName = prefs.getString(KEY_ROOM_NAME, null).orEmpty(),
        minAnswers = prefs.getInt(KEY_MIN_ANSWERS, GameSettings.DEFAULT_MIN_ANSWERS),
        maxAnswers = prefs.getInt(KEY_MAX_ANSWERS, GameSettings.MAX_ANSWERS),
        teamNames = mapOf(
            TeamId.TEAM_1 to prefs.getString(KEY_TEAM_1, null).orEmpty(),
            TeamId.TEAM_2 to prefs.getString(KEY_TEAM_2, null).orEmpty()
        ),
        bankName = prefs.getString(KEY_BANK_NAME, null)?.takeIf { bankFile.exists() },
        bankQuestionCount = prefs.getInt(KEY_BANK_COUNT, 0)
    ).clamped()

    fun save(settings: GameSettings) {
        val safe = settings.clamped()
        prefs.edit()
            .putInt(KEY_ROUNDS, safe.rounds)
            .putString(KEY_MULTIPLIERS, safe.multipliers.joinToString(","))
            .putInt(KEY_STRIKES, safe.strikesToSteal)
            .putInt(KEY_ANSWER_SECONDS, safe.answerSeconds)
            .putInt(KEY_CHOICE_SECONDS, safe.choiceSeconds)
            .putString(KEY_ROOM_NAME, safe.roomName)
            .putInt(KEY_MIN_ANSWERS, safe.minAnswers)
            .putInt(KEY_MAX_ANSWERS, safe.maxAnswers)
            .putString(KEY_TEAM_1, safe.teamName(TeamId.TEAM_1))
            .putString(KEY_TEAM_2, safe.teamName(TeamId.TEAM_2))
            .apply()
    }

    /** أسئلة اللعبة: بنك المضيف إذا مستورد، وإلا البنك المرفق. */
    fun questions(): List<Question> {
        if (!bankFile.exists()) return QuestionBank.load()
        return when (val result = QuestionBank.parse(bankFile.readText())) {
            is BankResult.Success -> result.questions
            is BankResult.Failure -> QuestionBank.load()
        }
    }

    /** حدود عدد الأجوبة الموجودة فعلياً بالبنك — منها بتتبنى خطوات الفلتر. */
    fun answerBounds(): IntRange {
        val sizes = questions().map { it.answers.size }
        val low = (sizes.minOrNull() ?: GameSettings.MIN_ANSWERS)
            .coerceIn(GameSettings.MIN_ANSWERS, GameSettings.MAX_ANSWERS)
        val high = (sizes.maxOrNull() ?: GameSettings.MAX_ANSWERS)
            .coerceIn(low, GameSettings.MAX_ANSWERS)
        return low..high
    }

    /** أسئلة البنك بعد فلتر عدد الأجوبة. */
    fun filteredQuestions(settings: GameSettings = load()): List<Question> =
        questions().filter { question ->
            question.answers.size in settings.minAnswers..settings.maxAnswers
        }

    /** الأسئلة اللي انقرأت قبل — ما بترجع لحد ما يخلص البنك. */
    fun readQuestionIds(): Set<String> = prefs.getStringSet(KEY_READ_IDS, emptySet()).orEmpty()

    /** المضيف شاف السؤال — منسجّله حتى ما يتكرر باللعبة الجاية. */
    fun markQuestionRead(id: String) {
        val bank = filteredQuestions().ifEmpty { questions() }
        val read = readQuestionIds() + id
        // خلصت كل الأسئلة؟ منبلّش دورة جديدة نظيفة.
        val next = if (bank.isNotEmpty() && bank.all { it.id in read || it.isRead }) {
            emptySet()
        } else {
            read
        }
        prefs.edit().putStringSet(KEY_READ_IDS, next).apply()
    }

    /** بنك جديد = دورة قراءة جديدة. */
    fun clearReadQuestions() {
        prefs.edit().remove(KEY_READ_IDS).apply()
    }

    /** حالة بداية للعبة جديدة: أسئلة ما انقرأت وإعدادات المضيف الحالية. */
    fun newGameState(): GameState {
        val settings = load()
        return GameState(
            questions = QuestionBank.randomGame(
                rounds = settings.rounds,
                // الفلتر تبع المضيف: عدد أجوبة معيّن.
                source = filteredQuestions(settings).ifEmpty { questions() },
                readIds = readQuestionIds()
            ),
            multipliers = settings.multipliersForRounds(),
            strikesToSteal = settings.strikesToSteal,
            answerLimitSeconds = settings.answerSeconds,
            choiceLimitSeconds = settings.choiceSeconds,
            teams = TeamId.entries.associateWith { id ->
                TeamState(id, settings.teamName(id))
            }
        )
    }

    /**
     * بيقرأ ملف اختاره المضيف، بيتأكد منه، وبيحفظه. بيرجّع رسالة الخطأ
     * إذا الملف مش صالح — وبهاي الحالة البنك القديم بيضل زي ما هو.
     */
    fun importBank(uri: Uri, displayName: String): BankResult {
        val text = try {
            appContext.contentResolver.openInputStream(uri)?.use {
                it.readBytes().decodeToString()
            } ?: return BankResult.Failure("ما قدرنا نفتح الملف")
        } catch (error: Exception) {
            return BankResult.Failure("ما قدرنا نقرأ الملف: ${error.message ?: "خطأ"}")
        }

        val result = QuestionBank.parse(text)
        if (result is BankResult.Success) {
            bankFile.writeText(text)
            clearReadQuestions()
            prefs.edit()
                .putString(KEY_BANK_NAME, displayName)
                .putInt(KEY_BANK_COUNT, result.questions.size)
                .apply()
        }
        return result
    }

    /** رجوع للبنك المرفق مع التطبيق. */
    fun clearBank() {
        clearReadQuestions()
        bankFile.delete()
        prefs.edit().remove(KEY_BANK_NAME).remove(KEY_BANK_COUNT).apply()
    }

    private companion object {
        const val BANK_FILE = "host_bank.json"
        const val KEY_ROUNDS = "rounds"
        const val KEY_MULTIPLIERS = "multipliers"
        const val KEY_STRIKES = "strikes"
        const val KEY_ANSWER_SECONDS = "answer_seconds"
        const val KEY_CHOICE_SECONDS = "choice_seconds"
        const val KEY_ROOM_NAME = "room_name"
        const val KEY_READ_IDS = "read_question_ids"
        const val KEY_MIN_ANSWERS = "min_answers"
        const val KEY_MAX_ANSWERS = "max_answers"
        const val KEY_TEAM_1 = "team_1_name"
        const val KEY_TEAM_2 = "team_2_name"
        const val KEY_BANK_NAME = "bank_name"
        const val KEY_BANK_COUNT = "bank_count"
    }
}
