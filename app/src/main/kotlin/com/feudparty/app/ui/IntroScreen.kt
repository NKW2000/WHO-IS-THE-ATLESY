package com.feudparty.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.BrandBadge
import com.feudparty.app.ui.components.BrandBar
import com.feudparty.app.ui.components.BrandWordmark
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import com.feudparty.app.feedback.Cue
import com.feudparty.app.feedback.SceneCue
import kotlin.math.cos
import kotlin.math.sin

/**
 * المقدمة المتحركة — نفس السيناريو والتوقيت اللي بملف التصميم
 * (`intro-bumper.jsx` + مشاهد `OM_SCENES`)، أربع ثواني:
 *
 * | المشهد | المدة | شو بيصير |
 * |---|---|---|
 * | Flash | ٠٫٥٥ | أرضية ذهبية، ذرات حبرية، وشعاع بيمسح الكادر |
 * | Clash | ٠٫٨٥ | لوح أخضر ولوح أزرق بيتصادموا، موجة صدمة، و«؟» بتنط |
 * | Build | ١٫٤٥ | «؟» بتصير شارة، واللوح البنفسجي والاسم بينطبقوا |
 * | Win | ١٫١٥ | بريق ذهبي، انفجار قصاصات، أجنحة الفريقين، وارتدادة القفلة |
 *
 * الوقت `t` بالثواني، وكل حركة مربوطة فيه زي المحرّك بالتصميم. أي لمسة
 * بتخطّي المقدمة.
 */
private const val FLASH = 0f
private const val CLASH = 0.55f
private const val BUILD = 1.40f
private const val WIN = 2.85f
private const val TOTAL = 4.0f

/** نفس منحنيات التصميم: خروج سريع، ارتداد، ودخول متسارع. */
private fun ease(t: Float): Float = 1f - (1f - t) * (1f - t) * (1f - t) * (1f - t)
private fun easeIn(t: Float): Float = t * t
private fun easeBack(t: Float): Float {
    val c = 1.70158f
    val p = t - 1f
    return p * p * ((c + 1f) * p + c) + 1f
}

/** قيمة متحرّكة بين [from] و[to] خلال [start]..[end] بالثواني. */
private fun span(
    t: Float,
    from: Float,
    to: Float,
    start: Float,
    end: Float,
    curve: (Float) -> Float = ::ease
): Float {
    if (t <= start) return from
    if (t >= end) return to
    val p = ((t - start) / (end - start)).coerceIn(0f, 1f)
    return from + (to - from) * curve(p)
}

private fun rnd(index: Int, salt: Float): Float {
    val v = sin(index * salt) * 43758.547f
    return v - kotlin.math.floor(v)
}

private class Speck(val x: Float, val y: Float, val size: Float, val depth: Float)
private class Spark(
    val angle: Float,
    val radius: Float,
    val size: Float,
    val color: Color,
    val square: Boolean
)

@Composable
fun IntroScreen(onDone: () -> Unit) {
    // موسيقى المقدمة — موقّتة على نفس ضربات المشهد: وميض، تصادم، بناء، فوز.
    SceneCue(Cue.INTRO)

    // بالوضع الطولي في مقدمة خاصة — نفس السيناريو بس بتصادم عمودي
    // واسم بسطرين، زي ملف التصميم الطولي.
    if (isPortrait()) {
        IntroPortraitScreen(onDone = onDone)
        return
    }

    val finish by rememberUpdatedState(onDone)
    var t by remember { mutableFloatStateOf(0f) }

    // بتلف على حالها زي `OM_PLAYBACK: loop` بالتصميم: باللعبة `onDone`
    // بتنقّل للشاشة الرئيسية فبتوقف، وبالمعرض بتضل تعيد.
    LaunchedEffect(Unit) {
        var start = withFrameNanos { it }
        var announced = false
        while (true) {
            val now = withFrameNanos { it }
            t = (now - start) / 1_000_000_000f
            if (t >= TOTAL) {
                if (!announced) {
                    announced = true
                    finish()
                }
                start = now
                t = 0f
            }
        }
    }

    val specks = remember {
        List(40) { i ->
            Speck(rnd(i + 1, 12.9898f), rnd(i + 1, 78.233f), 1.5f + rnd(i + 1, 45.164f) * 3.5f, rnd(i + 1, 94.673f))
        }
    }
    val sparks = remember {
        val palette = listOf(
            FeudColors.gold, FeudColors.teal, FeudColors.team1, FeudColors.pink, FeudColors.lime
        )
        List(26) { i ->
            Spark(
                angle = (i / 26f) * Math.PI.toFloat() * 2f + rnd(i + 3, 9.31f) * 0.5f,
                radius = 190f + rnd(i + 3, 31.7f) * 210f,
                size = 7f + rnd(i + 3, 52.1f) * 13f,
                color = palette[i % palette.size],
                square = rnd(i + 3, 17.4f) > 0.5f
            )
        }
    }

    // كاميرا: زووم خفيف عند التصادم، وسحبة للورا وقت البناء، وارتدادة بالآخر.
    val impact = CLASH + 0.32f
    val cam = 1f +
        span(t, 0.06f, 0f, FLASH, CLASH) +
        span(t, 0f, 0.10f, CLASH + 0.18f, impact) +
        span(t, 0f, -0.02f, impact, impact + 0.36f) +
        span(t, 0f, -0.08f, BUILD, BUILD + 0.55f) +
        span(t, 0f, 0.035f, WIN + 0.55f, WIN + 0.70f) +
        span(t, 0f, -0.035f, WIN + 0.70f, WIN + 1.05f)

    val beam = span(t, -0.35f, 1.35f, FLASH, CLASH + 0.3f)
    val beamAlpha = span(t, 0f, 0.85f, FLASH, FLASH + 0.22f) + span(t, 0f, -0.85f, CLASH, CLASH + 0.3f)
    val dust = span(t, 0f, 1f, FLASH, FLASH + 0.35f)

    val teamIn = span(t, 620f, 0f, CLASH, impact, ::easeIn)
    val part = span(t, 0f, 820f, impact + 0.1f, BUILD + 0.35f, ::easeIn)
    val teamAlpha = 1f + span(t, 0f, -1f, BUILD + 0.05f, BUILD + 0.30f)
    val spin = span(t, 0f, 18f, impact + 0.1f, BUILD + 0.35f, ::easeIn)
    val squash = span(t, 0f, 1f, impact, impact + 0.08f) + span(t, 0f, -1f, impact + 0.08f, impact + 0.30f)
    val shock = span(t, 0.2f, 3.4f, impact, impact + 0.4f)
    val shockAlpha = span(t, 1f, 0f, impact + 0.04f, impact + 0.4f)
    val markPop = span(t, 0f, 1f, impact + 0.04f, impact + 0.28f, ::easeBack)
    val markGone = span(t, 1f, 0f, BUILD, BUILD + 0.16f, ::easeIn)

    val plate = span(t, 0f, 1f, BUILD, BUILD + 0.36f, ::easeBack)
    val badge = span(t, 0f, 1f, BUILD + 0.08f, BUILD + 0.50f, ::easeBack)
    val badgeSpin = span(t, -40f, 0f, BUILD + 0.08f, BUILD + 0.56f, ::easeBack)
    val wordIn = span(t, 460f, 0f, BUILD + 0.34f, BUILD + 0.78f, ::easeBack)
    val wordAlpha = span(t, 0f, 1f, BUILD + 0.34f, BUILD + 0.44f)
    val barGrow = span(t, 0f, 1f, BUILD + 0.86f, BUILD + 1.14f)
    val logoSquash = span(t, 0f, 1f, BUILD + 1.0f, BUILD + 1.08f) +
        span(t, 0f, -1f, BUILD + 1.08f, BUILD + 1.38f)

    val burst = span(t, 0f, 1f, WIN + 0.06f, WIN + 0.85f)
    val burstAlpha = span(t, 1f, 0f, WIN + 0.38f, WIN + 0.90f)
    val wing = span(t, 230f, 0f, WIN + 0.04f, WIN + 0.42f, ::easeBack)
    val bob = sin(t * 5.2f) * 4f

    val interaction = remember { MutableInteractionSource() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FeudColors.gold)
            .clickable(interactionSource = interaction, indication = null) { finish() }
    ) {
        val w = maxWidth
        val h = maxHeight

        // ذرات حبرية على الأرضية الذهبية.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    specks.forEach { s ->
                        val y = (s.y * size.height + sin(t * 1.6f + s.depth * 9f) * 22f)
                            .mod(size.height)
                        drawCircle(
                            color = FeudColors.ink.copy(alpha = dust * (0.1f + s.depth * 0.22f)),
                            radius = s.size,
                            center = Offset(s.x * size.width, y)
                        )
                    }
                }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = cam; scaleY = cam },
            contentAlignment = Alignment.Center
        ) {
            // شعاع الضوء اللي بيمسح الكادر.
            if (beamAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .offset(x = w * beam - 80.dp, y = -h)
                        .width(160.dp)
                        .height(h * 3)
                        .rotate(14f)
                        .alpha(beamAlpha.coerceIn(0f, 1f))
                        .background(FeudColors.cream.copy(alpha = 0.5f))
                )
            }

            // الفريقين — بيتصادموا وبعدين بيفترقوا.
            if (teamAlpha > 0.01f) {
                TeamBlock(
                    color = FeudColors.team1,
                    offsetX = -(teamIn + part).dp,
                    rotation = -spin,
                    squash = squash,
                    alpha = teamAlpha
                )
                TeamBlock(
                    color = FeudColors.team2,
                    offsetX = (teamIn + part).dp,
                    rotation = spin,
                    squash = squash,
                    alpha = teamAlpha
                )
            }

            // موجة الصدمة.
            if (shockAlpha > 0.01f && t > impact) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(shock)
                        .alpha(shockAlpha.coerceIn(0f, 1f))
                        .border(10.dp, FeudColors.cream, RoundedCornerShape(percent = 50))
                )
            }

            // «؟» الكبيرة قبل ما تصير شارة.
            val markScale = markPop * markGone
            if (markScale > 0.01f) {
                BrandBadge(
                    em = 180.dp,
                    modifier = Modifier.scale(markScale)
                )
            }

            // أجنحة الفريقين بمشهد الفوز.
            if (t >= WIN) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        WingBlock(
                            color = FeudColors.team1,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = (-86).dp + wing.dp, y = bob.dp)
                                .rotate(-6f)
                        )
                        WingBlock(
                            color = FeudColors.team2,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = 86.dp - wing.dp, y = (-bob).dp)
                                .rotate(6f)
                        )
                    }
                }
            }

            // اللوح والعلامة: اللوح بيلبس المحتوى نفسه، فبيكبر معه بدل
            // ما يكون مستطيل فاضي وراه.
            if (plate > 0.01f) {
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = plate * (1f + logoSquash * 0.06f)
                            scaleY = plate * (1f - logoSquash * 0.05f)
                        }
                        .drawBehind {
                            drawRoundRect(
                                color = FeudColors.ink,
                                topLeft = Offset(14f, 17f),
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(34f)
                            )
                        }
                        .background(FeudColors.canvas, RoundedCornerShape(28.dp))
                        .border(6.dp, FeudColors.ink, RoundedCornerShape(28.dp))
                        .width(560.dp)
                        .height(260.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    BrandBadge(
                        em = 68.dp,
                        modifier = Modifier
                            .scale(badge)
                            .rotate(badgeSpin)
                    )
                    Spacer(Modifier.height(16.dp))
                    BrandWordmark(
                        em = 42.dp,
                        modifier = Modifier
                            .offset(x = wordIn.dp)
                            .alpha(wordAlpha.coerceIn(0f, 1f))
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(modifier = Modifier.scale(barGrow.coerceIn(0f, 1f))) {
                        BrandBar(em = 42.dp)
                    }
                }
            }

            // انفجار القصاصات بمشهد الفوز.
            if (burst > 0f && burstAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val origin = Offset(size.width / 2f, size.height / 2f - 20f)
                            sparks.forEach { s ->
                                val point = Offset(
                                    origin.x + cos(s.angle) * s.radius * burst,
                                    origin.y + sin(s.angle) * s.radius * burst
                                )
                                val color = s.color.copy(alpha = burstAlpha.coerceIn(0f, 1f))
                                val ink = FeudColors.ink.copy(alpha = burstAlpha.coerceIn(0f, 1f))
                                if (s.square) {
                                    drawRoundRect(
                                        color = ink,
                                        topLeft = Offset(point.x - s.size / 2f - 3f, point.y - s.size / 2f - 3f),
                                        size = Size(s.size + 6f, s.size + 6f),
                                        cornerRadius = CornerRadius(3f)
                                    )
                                    drawRoundRect(
                                        color = color,
                                        topLeft = Offset(point.x - s.size / 2f, point.y - s.size / 2f),
                                        size = Size(s.size, s.size),
                                        cornerRadius = CornerRadius(3f)
                                    )
                                } else {
                                    drawCircle(ink, s.size / 2f + 3f, point)
                                    drawCircle(color, s.size / 2f, point)
                                }
                            }
                        }
                )
            }
        }
    }
}

/** لوح فريق: مستطيل ملوّن بحدّ حبر وظل صلب — نفس بلوكات التصميم. */
@Composable
private fun TeamBlock(
    color: Color,
    offsetX: androidx.compose.ui.unit.Dp,
    rotation: Float,
    squash: Float,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .rotate(rotation)
            .graphicsLayer {
                scaleX = 1f + squash * 0.28f
                scaleY = 1f - squash * 0.22f
                this.alpha = alpha.coerceIn(0f, 1f)
            }
            .size(width = 172.dp, height = 112.dp)
            .drawBehind {
                drawRoundRect(
                    color = FeudColors.ink,
                    topLeft = Offset(10f, 12f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(28f)
                )
            }
            .background(color, RoundedCornerShape(24.dp))
            .border(5.dp, FeudColors.ink, RoundedCornerShape(24.dp))
    )
}

/** جناح جانبي بمشهد الفوز. */
@Composable
private fun WingBlock(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 120.dp, height = 168.dp)
            .background(color, RoundedCornerShape(20.dp))
            .border(5.dp, FeudColors.ink, RoundedCornerShape(20.dp))
    )
}

@Preview(widthDp = 880, heightDp = 420)
@Composable
private fun IntroScreenPreview() {
    FeudPartyTheme { IntroScreen(onDone = {}) }
}
