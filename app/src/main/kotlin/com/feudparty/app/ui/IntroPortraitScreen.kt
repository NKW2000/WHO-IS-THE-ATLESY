package com.feudparty.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feudparty.app.ui.components.BrandBadge
import com.feudparty.app.ui.components.BrandBar
import com.feudparty.app.ui.components.BrandWordLine
import com.feudparty.app.ui.theme.FeudColors
import com.feudparty.app.ui.theme.FeudPartyTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * المقدمة المتحركة بالوضع الطولي — نفس سيناريو وتوقيت ملف التصميم
 * (`مين الأطليسي - أنترو طولي` + `intro-bumper-portrait.jsx`)، ٧٫٦ ثانية:
 *
 * | المشهد | المدة | شو بيصير |
 * |---|---|---|
 * | Flash | ١٫١ | أرضية ذهبية، ذرات حبرية، وشعاع بينزل بطول الكادر |
 * | Clash | ١٫٥ | لوح أخضر من فوق وأزرق من تحت بيتصادموا، موجة صدمة، و«؟» بتنط |
 * | Build | ٢٫٦ | «؟» بتصير شارة، والاسم بينطبق بسطرين على اللوح البنفسجي |
 * | Win | ٢٫٤ | بريق ذهبي، الكأس لحظة، قصاصات، وشريطا الفريقين والقفلة |
 *
 * أي لمسة بتخطّي المقدمة.
 */
private const val P_FLASH = 0f
private const val P_CLASH = 1.1f
private const val P_BUILD = 2.6f
private const val P_WIN = 5.2f
private const val P_TOTAL = 7.6f
private const val P_IMPACT = P_CLASH + 0.62f

private fun pEase(t: Float): Float = 1f - (1f - t) * (1f - t) * (1f - t) * (1f - t)
private fun pEaseIn(t: Float): Float = t * t
private fun pEaseBack(t: Float): Float {
    val c = 1.70158f
    val p = t - 1f
    return p * p * ((c + 1f) * p + c) + 1f
}

private fun pSpan(
    t: Float,
    from: Float,
    to: Float,
    start: Float,
    end: Float,
    curve: (Float) -> Float = ::pEase
): Float {
    if (t <= start) return from
    if (t >= end) return to
    val p = ((t - start) / (end - start)).coerceIn(0f, 1f)
    return from + (to - from) * curve(p)
}

private fun pRnd(index: Int, salt: Float): Float {
    val v = sin(index * salt) * 43758.547f
    return v - kotlin.math.floor(v)
}

private class Dust(val x: Float, val y: Float, val size: Float, val depth: Float)
private class Confetto(
    val angle: Float,
    val radius: Float,
    val size: Float,
    val color: Color,
    val square: Boolean
)

@Composable
fun IntroPortraitScreen(onDone: () -> Unit) {
    val finish by rememberUpdatedState(onDone)
    var t by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var start = withFrameNanos { it }
        var announced = false
        while (true) {
            val now = withFrameNanos { it }
            t = (now - start) / 1_000_000_000f
            if (t >= P_TOTAL) {
                if (!announced) {
                    announced = true
                    finish()
                }
                start = now
                t = 0f
            }
        }
    }

    val dust = remember {
        List(38) { i ->
            Dust(
                x = pRnd(i + 1, 12.9898f),
                y = pRnd(i + 1, 78.233f),
                size = 1.5f + pRnd(i + 1, 45.164f) * 3.5f,
                depth = pRnd(i + 1, 94.673f)
            )
        }
    }
    val confetti = remember {
        val palette = listOf(
            FeudColors.gold, FeudColors.teal, FeudColors.team1, FeudColors.pink, FeudColors.lime
        )
        List(26) { i ->
            Confetto(
                angle = (i / 26f) * Math.PI.toFloat() * 2f + pRnd(i + 3, 9.31f) * 0.5f,
                radius = 0.315f + pRnd(i + 3, 31.7f) * 0.426f,
                size = 7f + pRnd(i + 3, 52.1f) * 13f,
                color = palette[i % palette.size],
                square = pRnd(i + 3, 17.4f) > 0.5f
            )
        }
    }

    // كاميرا: زووم خفيف عند التصادم وسحبة بالبناء وارتدادة بالقفلة.
    val cam = 1f +
        pSpan(t, 0.06f, 0f, P_FLASH, P_CLASH) +
        pSpan(t, 0f, 0.10f, P_CLASH + 0.3f, P_IMPACT) +
        pSpan(t, 0f, -0.02f, P_IMPACT, P_IMPACT + 0.6f) +
        pSpan(t, 0f, -0.06f, P_BUILD, P_BUILD + 0.9f) +
        pSpan(t, 0f, 0.035f, P_WIN + 0.9f, P_WIN + 1.15f) +
        pSpan(t, 0f, -0.035f, P_WIN + 1.15f, P_WIN + 1.75f)

    val beam = pSpan(t, -0.35f, 1.35f, P_FLASH, P_CLASH + 0.55f)
    val beamAlpha = pSpan(t, 0f, 0.85f, P_FLASH, P_FLASH + 0.4f) +
        pSpan(t, 0f, -0.85f, P_CLASH, P_CLASH + 0.55f)
    val dustAlpha = pSpan(t, 0f, 1f, P_FLASH, P_FLASH + 0.6f)

    // الفريقين: أخضر من فوق وأزرق من تحت.
    val teamTop = pSpan(t, -1.33f, 0f, P_CLASH, P_IMPACT, ::pEaseIn)
    val teamBottom = pSpan(t, 1.33f, 0f, P_CLASH, P_IMPACT, ::pEaseIn)
    val squash = pSpan(t, 0f, 1f, P_IMPACT, P_IMPACT + 0.14f) +
        pSpan(t, 0f, -1f, P_IMPACT + 0.14f, P_IMPACT + 0.55f)
    val partTop = pSpan(t, 0f, -1.67f, P_IMPACT + 0.2f, P_BUILD + 0.6f, ::pEaseIn)
    val partBottom = pSpan(t, 0f, 1.67f, P_IMPACT + 0.2f, P_BUILD + 0.6f, ::pEaseIn)
    val teamAlpha = 1f + pSpan(t, 0f, -1f, P_BUILD + 0.1f, P_BUILD + 0.5f)
    val teamSpin = pSpan(t, 0f, 14f, P_IMPACT + 0.2f, P_BUILD + 0.6f, ::pEaseIn)
    val shock = pSpan(t, 0.2f, 3.4f, P_IMPACT, P_IMPACT + 0.7f)
    val shockAlpha = pSpan(t, 1f, 0f, P_IMPACT + 0.08f, P_IMPACT + 0.7f)
    val markPop = pSpan(t, 0f, 1f, P_IMPACT + 0.08f, P_IMPACT + 0.5f, ::pEaseBack)
    val markGone = pSpan(t, 1f, 0f, P_BUILD, P_BUILD + 0.3f, ::pEaseIn)

    // البناء: اللوح والشارة والاسم بسطرين.
    val plate = pSpan(t, 0f, 1f, P_BUILD, P_BUILD + 0.6f, ::pEaseBack)
    val badge = pSpan(t, 0f, 1f, P_BUILD + 0.15f, P_BUILD + 0.85f, ::pEaseBack)
    val badgeSpin = pSpan(t, -40f, 0f, P_BUILD + 0.15f, P_BUILD + 0.95f, ::pEaseBack)
    val line1 = pSpan(t, -0.48f, 0f, P_BUILD + 0.7f, P_BUILD + 1.4f, ::pEaseBack)
    val line1Rot = pSpan(t, 10f, 0f, P_BUILD + 0.7f, P_BUILD + 1.5f, ::pEaseBack)
    val line1Alpha = pSpan(t, 0f, 1f, P_BUILD + 0.7f, P_BUILD + 0.9f)
    val line2 = pSpan(t, 0.48f, 0f, P_BUILD + 1.1f, P_BUILD + 1.85f, ::pEaseBack)
    val line2Rot = pSpan(t, -10f, 0f, P_BUILD + 1.1f, P_BUILD + 1.95f, ::pEaseBack)
    val line2Alpha = pSpan(t, 0f, 1f, P_BUILD + 1.1f, P_BUILD + 1.3f)
    val barGrow = pSpan(t, 0f, 1f, P_BUILD + 1.7f, P_BUILD + 2.1f)
    val logoSquash = pSpan(t, 0f, 1f, P_BUILD + 1.9f, P_BUILD + 2.05f) +
        pSpan(t, 0f, -1f, P_BUILD + 2.05f, P_BUILD + 2.6f)

    // الفوز: قصاصات وأجنحة وسطر التعريف.
    val burst = pSpan(t, 0f, 1f, P_WIN + 0.15f, P_WIN + 1.5f)
    val burstAlpha = pSpan(t, 1f, 0f, P_WIN + 0.7f, P_WIN + 1.6f)
    val wingTop = pSpan(t, -1f, 0f, P_WIN + 0.1f, P_WIN + 0.8f, ::pEaseBack)
    val wingBottom = pSpan(t, 1f, 0f, P_WIN + 0.1f, P_WIN + 0.8f, ::pEaseBack)
    val tagAlpha = pSpan(t, 0f, 1f, P_WIN + 0.45f, P_WIN + 0.9f)
    val tagY = pSpan(t, 0.035f, 0f, P_WIN + 0.45f, P_WIN + 1.0f, ::pEaseBack)
    val bob = sin(t * 3.2f) * 4f

    val interaction = remember { MutableInteractionSource() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FeudColors.gold)
            .clickable(interactionSource = interaction, indication = null) { finish() }
    ) {
        val w = maxWidth
        val h = maxHeight
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    dust.forEach { speck ->
                        val y = (speck.y * size.height + sin(t * 1.1f + speck.depth * 9f) * 26f)
                            .mod(size.height)
                        drawCircle(
                            color = FeudColors.ink.copy(
                                alpha = dustAlpha * (0.1f + speck.depth * 0.22f)
                            ),
                            radius = speck.size,
                            center = Offset(speck.x * size.width, y)
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
            // شعاع الضوء بينزل بطول الكادر.
            if (beamAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = h * beam - h * 0.157f)
                        .width(w * 2f)
                        .height(h * 0.315f)
                        .rotate(-12f)
                        .alpha(beamAlpha.coerceIn(0f, 1f))
                        .background(FeudColors.cream.copy(alpha = 0.5f))
                )
            }

            // الفريقين بيتصادموا عمودياً وبعدين بيفترقوا.
            if (teamAlpha > 0.01f) {
                VerticalTeamBlock(
                    color = FeudColors.team1,
                    width = w * 0.74f,
                    height = h * 0.231f,
                    offsetY = h * (teamTop + partTop) - h * 0.111f,
                    rotation = -teamSpin,
                    squash = squash,
                    alpha = teamAlpha
                )
                VerticalTeamBlock(
                    color = FeudColors.team2,
                    width = w * 0.74f,
                    height = h * 0.231f,
                    offsetY = h * (teamBottom + partBottom) + h * 0.111f,
                    rotation = teamSpin,
                    squash = squash,
                    alpha = teamAlpha
                )
            }

            if (shockAlpha > 0.01f && t > P_IMPACT) {
                Box(
                    modifier = Modifier
                        .size(w * 0.26f)
                        .scale(shock)
                        .alpha(shockAlpha.coerceIn(0f, 1f))
                        .border(12.dp, FeudColors.cream, RoundedCornerShape(percent = 50))
                )
            }

            val markScale = markPop * markGone
            if (markScale > 0.01f) {
                BrandBadge(em = w * 0.5f, modifier = Modifier.scale(markScale))
            }

            // أجنحة الفريقين: وحدة فوق ووحدة تحت.
            if (t >= P_WIN) {
                Box(modifier = Modifier.fillMaxSize()) {
                    WingBar(
                        color = FeudColors.team1,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = -h * 0.111f + h * 0.185f * wingTop, x = bob.dp)
                            .padding(horizontal = w * 0.111f)
                            .fillMaxWidth()
                            .height(h * 0.185f)
                            .rotate(-3f)
                    )
                    WingBar(
                        color = FeudColors.team2,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = h * 0.111f + h * 0.185f * wingBottom, x = (-bob).dp)
                            .padding(horizontal = w * 0.111f)
                            .fillMaxWidth()
                            .height(h * 0.185f)
                            .rotate(3f)
                    )
                }
            }

            // اللوح البنفسجي: الشارة، الاسم بسطرين، الشريط الذهبي، والتعريف.
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
                                cornerRadius = CornerRadius(with(density) { 38.dp.toPx() })
                            )
                        }
                        .background(FeudColors.canvas, RoundedCornerShape(38.dp))
                        .border(8.dp, FeudColors.ink, RoundedCornerShape(38.dp))
                        .width(w * 0.815f)
                        .height(h * 0.574f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    BrandBadge(
                        em = w * 0.274f,
                        modifier = Modifier
                            .scale(badge)
                            .rotate(badgeSpin)
                    )
                    Spacer(Modifier.height(h * 0.024f))
                    BrandWordLine(
                        text = "مين",
                        em = w * 0.178f,
                        modifier = Modifier
                            .offset(y = h * line1)
                            .rotate(line1Rot)
                            .alpha(line1Alpha.coerceIn(0f, 1f))
                    )
                    Spacer(Modifier.height(h * 0.009f))
                    BrandWordLine(
                        text = "الأطليسي",
                        em = w * 0.178f,
                        modifier = Modifier
                            .offset(y = h * line2)
                            .rotate(line2Rot)
                            .alpha(line2Alpha.coerceIn(0f, 1f))
                    )
                    Spacer(Modifier.height(h * 0.024f))
                    Box(modifier = Modifier.scale(barGrow.coerceIn(0f, 1f))) {
                        BrandBar(em = w * 0.135f)
                    }
                    if (tagAlpha > 0.01f) {
                        Spacer(Modifier.height(h * 0.018f))
                        Text(
                            "لعبة عائلية · فريقين · جهاز لكل لاعب",
                            color = FeudColors.cream,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .offset(y = h * tagY)
                                .alpha(tagAlpha.coerceIn(0f, 1f))
                        )
                    }
                }
            }

            // قصاصات الفوز.
            if (burst > 0f && burstAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val origin = Offset(size.width / 2f, size.height / 2f - 20f)
                            confetti.forEach { piece ->
                                val point = Offset(
                                    origin.x + cos(piece.angle) * piece.radius *
                                        size.width * burst,
                                    origin.y + sin(piece.angle) * piece.radius *
                                        size.width * burst * 1.6f
                                )
                                val alpha = burstAlpha.coerceIn(0f, 1f)
                                val color = piece.color.copy(alpha = alpha)
                                val ink = FeudColors.ink.copy(alpha = alpha)
                                if (piece.square) {
                                    drawRoundRect(
                                        color = ink,
                                        topLeft = Offset(
                                            point.x - piece.size / 2f - 3f,
                                            point.y - piece.size / 2f - 3f
                                        ),
                                        size = Size(piece.size + 6f, piece.size + 6f),
                                        cornerRadius = CornerRadius(3f)
                                    )
                                    drawRoundRect(
                                        color = color,
                                        topLeft = Offset(
                                            point.x - piece.size / 2f,
                                            point.y - piece.size / 2f
                                        ),
                                        size = Size(piece.size, piece.size),
                                        cornerRadius = CornerRadius(3f)
                                    )
                                } else {
                                    drawCircle(ink, piece.size / 2f + 3f, point)
                                    drawCircle(color, piece.size / 2f, point)
                                }
                            }
                        }
                )
            }
        }
    }
}

/** لوح فريق عمودي — بينزل من فوق أو بيطلع من تحت. */
@Composable
private fun VerticalTeamBlock(
    color: Color,
    width: Dp,
    height: Dp,
    offsetY: Dp,
    rotation: Float,
    squash: Float,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .offset(y = offsetY)
            .rotate(rotation)
            .graphicsLayer {
                scaleX = 1f - squash * 0.22f
                scaleY = 1f + squash * 0.28f
                this.alpha = alpha.coerceIn(0f, 1f)
            }
            .size(width = width, height = height)
            .drawBehind {
                drawRoundRect(
                    color = FeudColors.ink,
                    topLeft = Offset(10f, 12f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(30f)
                )
            }
            .background(color, RoundedCornerShape(30.dp))
            .border(7.dp, FeudColors.ink, RoundedCornerShape(30.dp))
    )
}

/** شريط فريق بمشهد الفوز — فوق وتحت الكادر. */
@Composable
private fun WingBar(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(26.dp))
            .border(7.dp, FeudColors.ink, RoundedCornerShape(26.dp))
    )
}

@Preview(widthDp = 385, heightDp = 770)
@Composable
private fun IntroPortraitPreview() {
    FeudPartyTheme { IntroPortraitScreen(onDone = {}) }
}
