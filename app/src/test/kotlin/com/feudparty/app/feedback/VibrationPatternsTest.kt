package com.feudparty.app.feedback

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VibrationPatternsTest {

    /**
     * هاد الاختبار موجود بسبب عطل حقيقي: `Cue.INTRO` كان نمطه
     * `longArrayOf(0, 0)` بمعنى «بدون اهتزاز»، و`createWaveform` رمت
     * استثناء فطار التطبيق أول ما يفتح — لأنه المقدمة أول شاشة.
     */
    @Test
    fun `ما في نمط كله أصفار`() {
        Cue.entries.forEach { cue ->
            val timings = vibrationPattern(cue) ?: return@forEach
            assertTrue(
                "$cue: نمط كله أصفار — createWaveform بترميه وبيطير التطبيق",
                timings.any { it > 0 }
            )
        }
    }

    /** كل نمط بيبلّش بصفر (انتظار) وبعدها نبضة، فالطول لازم يكون زوجي. */
    @Test
    fun `كل نمط طوله زوجي وبيبلّش بانتظار`() {
        Cue.entries.forEach { cue ->
            val timings = vibrationPattern(cue) ?: return@forEach
            assertTrue("$cue: طول فردي", timings.size % 2 == 0)
            assertTrue("$cue: ما بيبلّش بصفر", timings.first() == 0L)
        }
    }

    /** المشاهد الطويلة ودقّات الساعة بدون اهتزاز — بس بـ`null`. */
    @Test
    fun `المشاهد الطويلة بدون اهتزاز`() {
        listOf(Cue.INTRO, Cue.COUNT, Cue.CLOCK).forEach {
            assertNull("$it لازم يكون بدون اهتزاز", vibrationPattern(it))
        }
    }

    /** أحداث اللعب لازم تنحسّ بالإيد — هاي نص الفكرة. */
    @Test
    fun `أحداث اللعب إلها اهتزاز`() {
        listOf(Cue.REVEAL, Cue.BUZZ, Cue.WRONG, Cue.WIN, Cue.STRIKE_1).forEach {
            assertNotNull("$it لازم يهتز", vibrationPattern(it))
        }
    }
}
