package com.feudparty.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.feudparty.app.ui.theme.FeudColors

/**
 * أدوات حركة مشاهد البرنامج — نفس منحنيات ملف التصميم
 * (`مين الأطليسي - Play & Pass.dc.html`): كل حركة عبارة عن مفاتيح
 * (keyframes) على وقت بالثواني، ومنحنى `bang` هو
 * `cubic-bezier(.2,.9,.2,1)` تبع التصميم.
 */

/** ساعة المشهد بالثواني — بتبلّش من صفر كل ما يتغيّر [key]. */
@Composable
fun rememberShowClock(key: Any?, cap: Float = 12f): State<Float> {
    val time = remember(key) { mutableFloatStateOf(0f) }
    LaunchedEffect(key) {
        val start = withFrameNanos { it }
        while (time.floatValue < cap) {
            time.floatValue = withFrameNanos { (it - start) / 1_000_000_000f }
        }
    }
    return time
}

/** منحنى الضربة تبع التصميم — سريع بالبداية وبيهدى بالآخر. */
fun bang(p: Float): Float {
    // تقريب `cubic-bezier(.2,.9,.2,1)` بمنحنى أُسّي — نفس الإحساس.
    val t = p.coerceIn(0f, 1f)
    return 1f - (1f - t) * (1f - t) * (1f - t)
}

/**
 * قيمة بين مفاتيح: [stops] لازم تكون مرتّبة بنسبة الوقت `0f..1f`.
 * [t] بالثواني، الحركة بتبلّش بعد [delay] وبتاخد [duration].
 */
fun keys(
    t: Float,
    delay: Float,
    duration: Float,
    stops: List<Pair<Float, Float>>,
    curve: (Float) -> Float = ::bang
): Float {
    if (stops.isEmpty()) return 0f
    val raw = ((t - delay) / duration)
    if (raw <= 0f) return stops.first().second
    if (raw >= 1f) return stops.last().second
    val p = curve(raw)
    var previous = stops.first()
    for (stop in stops) {
        if (p <= stop.first) {
            val span = (stop.first - previous.first).takeIf { it > 0f } ?: return stop.second
            val local = (p - previous.first) / span
            return previous.second + (stop.second - previous.second) * local
        }
        previous = stop
    }
    return stops.last().second
}

/** `dcThump` — بتطلع من صفر، بتتخطّى، وبتستقر. */
fun thump(t: Float, delay: Float, duration: Float = 0.46f): Float = keys(
    t, delay, duration,
    listOf(0f to 0f, 0.52f to 1.22f, 0.76f to 0.94f, 1f to 1f)
)

/** `dcDrop` — بتنزل من فوق وبترتد. بترجّع الإزاحة كنسبة من الارتفاع. */
fun drop(t: Float, delay: Float, duration: Float = 0.62f): Float = keys(
    t, delay, duration,
    listOf(0f to -1.6f, 0.46f to 0.08f, 0.68f to -0.05f, 0.86f to 0.02f, 1f to 0f)
)

/** `dcWipe` — لافتة بتكنس من الجهة. بترجّع الإزاحة كنسبة من العرض. */
fun wipe(t: Float, delay: Float, duration: Float = 0.62f): Float = keys(
    t, delay, duration,
    listOf(0f to 1.12f, 0.54f to -0.06f, 0.72f to 0.03f, 0.88f to -0.015f, 1f to 0f)
)

/** `dcRise` — لوح بيطلع من تحت. إزاحة كنسبة من الارتفاع. */
fun rise(t: Float, delay: Float, duration: Float = 0.66f): Float = keys(
    t, delay, duration,
    listOf(0f to 1.18f, 0.62f to -0.04f, 0.82f to 0.02f, 1f to 0f)
)

/** `dcSlam` — بتنزل من كبير لصغير مع لفّة. بترجّع القياس. */
fun slamScale(t: Float, delay: Float, duration: Float = 0.7f): Float = keys(
    t, delay, duration,
    listOf(0f to 3.1f, 0.44f to 0.74f, 0.58f to 1.18f, 0.72f to 0.92f, 0.86f to 1.05f, 1f to 1f)
)

fun slamRotation(t: Float, delay: Float, duration: Float = 0.7f): Float = keys(
    t, delay, duration,
    listOf(0f to -16f, 0.44f to 4f, 0.58f to -3f, 0.72f to 2f, 0.86f to -1f, 1f to 0f)
)

/** بداية الظهور: شفافية الحركات اللي بتبلّش مخفية. */
fun appear(t: Float, delay: Float, over: Float = 0.12f): Float =
    ((t - delay) / over).coerceIn(0f, 1f)

/** `dcRing` — حلقة صدمة بتتوسّع وبتختفي. */
data class ShockRing(val scale: Float, val alpha: Float, val width: Float)

fun shockRing(t: Float, delay: Float, duration: Float = 0.76f): ShockRing {
    val raw = ((t - delay) / duration)
    if (raw <= 0f || raw >= 1f) return ShockRing(1f, 0f, 2f)
    val scale = keys(t, delay, duration, listOf(0f to 0.18f, 1f to 2.1f))
    val alpha = keys(
        t, delay, duration,
        listOf(0f to 0f, 0.06f to 1f, 0.55f to 0.55f, 1f to 0f), curve = { it }
    )
    val width = keys(t, delay, duration, listOf(0f to 28f, 1f to 2f))
    return ShockRing(scale, alpha, width)
}

/** خلفية الأشعة الدوّارة — نفس `repeating-conic-gradient` تبع التصميم. */
@Composable
fun SpinningRays(
    modifier: Modifier = Modifier,
    dark: Color = FeudColors.panelDark,
    light: Color = FeudColors.stage,
    periodMillis: Int = 14_000
) {
    val spin = rememberInfiniteTransition(label = "rays")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(periodMillis, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "raysAngle"
    )

    Canvas(modifier = modifier) {
        drawRect(dark)
        // ٢٤ شعاع بعرض ١٥ درجة — نفس تدرّج التصميم المخروطي.
        val radius = size.maxDimension * 1.4f
        rotate(angle) {
            repeat(12) { index ->
                drawArc(
                    color = light,
                    startAngle = index * 30f,
                    sweepAngle = 15f,
                    useCenter = true,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
            }
        }
    }
}
