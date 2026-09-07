package com.feudparty.app.demo

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.feudparty.app.feedback.ProvideGameFeedback
import com.feudparty.app.ui.theme.FeudPartyTheme

/**
 * نقطة دخول نسخة الديمو: بتفتح معرض الشاشات على طول، بدون صلاحيات ولا
 * اتصال. نفس إعدادات النافذة تبع اللعبة حتى يكون الشكل مطابق.
 */
class DemoActivity : ComponentActivity() {

    private fun goFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) goFullScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goFullScreen()
        setContent {
            FeudPartyTheme {
                ProvideGameFeedback {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        DemoGallery()
                    }
                }
            }
        }
    }
}
