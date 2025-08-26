
package nz.eloque.foss_wallet.ui.components

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityLock
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.utils.BiometricPromptManager

@Composable
fun VisibilityToggleButton(
    authStatus: Boolean,
    hideStatus: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context as FragmentActivity }
    val biometricPromptManager = remember { BiometricPromptManager(activity) }

    LaunchedEffect(biometricPromptManager) {
        biometricPromptManager.promptResults.collect { result ->
            when (result) {
                is BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                    onClick()
                }
                is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                    Toast.makeText(context, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show()
                }
                is BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                    Toast.makeText(context, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show()
                }
                is BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                    Toast.makeText(context, "Biometric hardware is unavailable.", Toast.LENGTH_SHORT).show()
                }
                is BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                    Toast.makeText(context, "Biometric feature is not available on this device.", Toast.LENGTH_SHORT).show()
                }
                is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                    Toast.makeText(context, "No biometric credentials are set up.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (hideStatus) {
        IconButton(onClick = onClick) {
            Icon(imageVector = Icons.Filled.VisibilityLock)
        }
    } else {
        IconButton(onClick = { 
            biometricPromptManager.showBiometricPrompt(
                title = stringResource(R.string.auth),
                description = stringResource(R.string.auth_description)
            )
        }) {
            Icon(imageVector = Icons.Filled.VisibilityOff)
        }
    }
}
