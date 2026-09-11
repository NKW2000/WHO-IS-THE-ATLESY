package com.feudparty.app.feedback

/**
 * نمط الاهتزاز لكل تنبيه، أو `null` إذا المشهد بدون اهتزاز.
 *
 * مهم: لازم يضل بكل نمط رقم أكبر من صفر.
 * `VibrationEffect.createWaveform` بترمي `IllegalArgumentException` إذا
 * كانت كل الأرقام أصفار، وهاد بيطيّر التطبيق. عشان هيك «بدون اهتزاز»
 * بتنكتب `null` مش `longArrayOf(0, 0)`.
 */
internal fun vibrationPattern(cue: Cue): LongArray? = when (cue) {
    // بدون اهتزاز: دقّات الساعة وأصوات المشاهد الطويلة — الجهاز بيضل
    // يزنّ طول الموسيقى بدون فايدة.
    Cue.CLOCK, Cue.INTRO, Cue.COUNT -> null

    Cue.REVEAL -> longArrayOf(0, 28)
    Cue.BUZZ -> longArrayOf(0, 18)
    Cue.STRIKE_1 -> longArrayOf(0, 60)
    Cue.STRIKE_2 -> longArrayOf(0, 60, 70, 60)
    Cue.STRIKE_3 -> longArrayOf(0, 70, 70, 70, 70, 140)
    Cue.WRONG -> longArrayOf(0, 130)
    Cue.WIN -> longArrayOf(0, 45, 60, 45, 60, 110)
    Cue.ROUND_START -> longArrayOf(0, 55)
    Cue.VERSUS -> longArrayOf(0, 40, 70, 70)
    Cue.CROWN -> longArrayOf(0, 30, 55, 30)
    Cue.FANFARE -> longArrayOf(0, 60, 70, 60, 70, 130)
    Cue.FIREWORK -> longArrayOf(0, 22)
    Cue.JOIN -> longArrayOf(0, 25)
    Cue.TAP -> longArrayOf(0, 12)
}
