package com.feudparty.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.feudparty.app.feedback.ProvideGameFeedback
import com.feudparty.app.navigation.FeudNavGraph
import com.feudparty.app.permissions.NearbyPermissions
import com.feudparty.app.ui.PermissionExplanationScreen
import com.feudparty.app.ui.theme.FeudBrushes
import com.feudparty.app.ui.theme.FeudPartyTheme

class MainActivity : ComponentActivity() {

    /**
     * لعبة على تلفزيون الصالون: بدون شريط حالة ولا شريط تنقّل. الأشرطة
     * بترجع مؤقتاً بسحبة من الحافة وبتختفي لحالها.
     */
    private fun goFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // الخلفية بتمتد تحت النتش، والمحتوى بينحط داخل المنطقة الآمنة.
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
        // بعد ما يرجع التركيز (إشعار، تبديل تطبيق) لازم نخفيهم من جديد.
        if (hasFocus) goFullScreen()
    }

    private var permissionsGranted by mutableStateOf(false)
    private var permanentlyDenied by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
        // لو المستخدم رفض بدون ما يطلع له طلب جديد، يعني رفض نهائي.
        permanentlyDenied = !permissionsGranted &&
            NearbyPermissions.required.none { shouldShowRequestPermissionRationale(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        goFullScreen()
        permissionsGranted = NearbyPermissions.allGranted(this)

        setContent {
            FeudPartyTheme {
                ProvideGameFeedback {
                // الخلفية بترسم على كل الشاشة — تحت النتش كمان — والمحتوى
                // لحاله هو اللي بيضل بالمنطقة الآمنة.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FeudBrushes.stage)
                ) {
                    if (permissionsGranted) {
                        FeudNavGraph()
                    } else {
                        PermissionExplanationScreen(
                            permanentlyDenied = permanentlyDenied,
                            onRequestClick = { permissionLauncher.launch(NearbyPermissions.required) },
                            onOpenSettings = ::openAppSettings
                        )
                    }
                }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // المستخدم ممكن يكون منح الصلاحية من الإعدادات وهو برا التطبيق.
        if (!permissionsGranted && NearbyPermissions.allGranted(this)) {
            permissionsGranted = true
            permanentlyDenied = false
        }
    }

    private fun openAppSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        )
    }
}
