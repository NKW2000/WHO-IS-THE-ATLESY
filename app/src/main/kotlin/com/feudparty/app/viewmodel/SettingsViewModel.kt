package com.feudparty.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.feudparty.app.settings.GameSettings
import com.feudparty.app.settings.SettingsRepository
import com.feudparty.core.game.Question
import com.feudparty.data.questions.BankResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** إعدادات المضيف — بتنحفظ فوراً مع كل تغيير. */
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _settings = MutableStateFlow(repository.load())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    /** آخر رسالة عن استيراد البنك — نجاح أو سبب الرفض. */
    private val _bankMessage = MutableStateFlow<String?>(null)
    val bankMessage: StateFlow<String?> = _bankMessage.asStateFlow()

    private val _bankFailed = MutableStateFlow(false)
    val bankFailed: StateFlow<Boolean> = _bankFailed.asStateFlow()

    fun update(settings: GameSettings) {
        val safe = settings.clamped()
        _settings.value = safe
        repository.save(safe)
    }

    fun importBank(uri: Uri, displayName: String) {
        when (val result = repository.importBank(uri, displayName)) {
            is BankResult.Success -> {
                _bankFailed.value = false
                _bankMessage.value = "تمام — انقرأ ${result.questions.size} سؤال من $displayName"
                _settings.value = repository.load()
            }

            is BankResult.Failure -> {
                _bankFailed.value = true
                _bankMessage.value = result.message
            }
        }
    }

    fun clearBank() {
        repository.clearBank()
        _bankFailed.value = false
        _bankMessage.value = "رجعنا للبنك المرفق مع التطبيق"
        _settings.value = repository.load()
    }

    /** الأسئلة المتاحة حالياً — بنك المضيف أو البنك المرفق. */
    fun questions(): List<Question> = repository.questions()

    /** تصنيفات البنك الحالي — للفلتر بشاشة الإعدادات. */
    fun categories(): List<String> = repository.categories()

    /** حدود عدد الأجوبة تبع البنك الحالي. */
    fun answerBounds(): IntRange = repository.answerBounds()

    /** كم سؤال بيطابق الفلتر الحالي. */
    fun matchingCount(settings: GameSettings): Int = repository.filteredQuestions(settings).size
}
