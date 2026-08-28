package com.feudparty.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.feudparty.app.navigation.FeudNavGraph
import com.feudparty.app.permissions.NearbyPermissions
import com.feudparty.app.ui.PermissionExplanationScreen
import com.feudparty.app.ui.theme.FeudPartyTheme

class MainActivity : ComponentActivity() {

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
        super.onCreate(savedInstanceState)
        permissionsGranted = NearbyPermissions.allGranted(this)

        setContent {
            FeudPartyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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
