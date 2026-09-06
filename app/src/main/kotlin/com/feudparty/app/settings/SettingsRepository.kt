package com.feudparty.app.settings

import android.content.Context
import android.net.Uri
import com.feudparty.core.game.Question
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
        fastMoneyEnabled = prefs.getBoolean(KEY_FM_ENABLED, true),
        fastMoneyTarget = prefs.getInt(KEY_FM_TARGET, GameSettings().fastMoneyTarget),
        fastMoneyFirstSeconds = prefs.getInt(KEY_FM_FIRST, GameSettings().fastMoneyFirstSeconds),
        fastMoneySecondSeconds = prefs.getInt(KEY_FM_SECOND, GameSettings().fastMoneySecondSeconds),
        bankName = prefs.getString(KEY_BANK_NAME, null)?.takeIf { bankFile.exists() },
        bankQuestionCount = prefs.getInt(KEY_BANK_COUNT, 0),
        lanTesting = prefs.getBoolean(KEY_LAN, false),
        lanHost = prefs.getString(KEY_LAN_HOST, GameSettings.DEFAULT_LAN_HOST)
            ?: GameSettings.DEFAULT_LAN_HOST
    ).clamped()

    fun save(settings: GameSettings) {
        val safe = settings.clamped()
        prefs.edit()
            .putInt(KEY_ROUNDS, safe.rounds)
            .putString(KEY_MULTIPLIERS, safe.multipliers.joinToString(","))
            .putInt(KEY_STRIKES, safe.strikesToSteal)
            .putBoolean(KEY_FM_ENABLED, safe.fastMoneyEnabled)
            .putInt(KEY_FM_TARGET, safe.fastMoneyTarget)
            .putInt(KEY_FM_FIRST, safe.fastMoneyFirstSeconds)
            .putInt(KEY_FM_SECOND, safe.fastMoneySecondSeconds)
            .putBoolean(KEY_LAN, safe.lanTesting)
            .putString(KEY_LAN_HOST, safe.lanHost)
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
        const val KEY_FM_ENABLED = "fm_enabled"
        const val KEY_FM_TARGET = "fm_target"
        const val KEY_FM_FIRST = "fm_first"
        const val KEY_FM_SECOND = "fm_second"
        const val KEY_BANK_NAME = "bank_name"
        const val KEY_BANK_COUNT = "bank_count"
        const val KEY_LAN = "lan_testing"
        const val KEY_LAN_HOST = "lan_host"
    }
}
