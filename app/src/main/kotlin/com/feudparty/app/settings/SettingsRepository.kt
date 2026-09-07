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

    /** حالة بداية للعبة جديدة: أسئلة مسحوبة عشوائي وإعدادات المضيف الحالية. */
    fun newGameState(): GameState {
        val settings = load()
        return GameState(
            questions = QuestionBank.randomGame(rounds = settings.rounds, source = questions()),
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
            prefs.edit()
                .putString(KEY_BANK_NAME, displayName)
                .putInt(KEY_BANK_COUNT, result.questions.size)
                .apply()
        }
        return result
    }

    /** رجوع للبنك المرفق مع التطبيق. */
    fun clearBank() {
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
        const val KEY_TEAM_1 = "team_1_name"
        const val KEY_TEAM_2 = "team_2_name"
        const val KEY_BANK_NAME = "bank_name"
        const val KEY_BANK_COUNT = "bank_count"
    }
}
