package nz.eloque.foss_wallet.utils

import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R

class Biometric(
    private val activity: FragmentActivity,
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) {
    private val resultChannel = Channel<BiometricResult>()

    fun prompt(
        description: String,
        onSuccess: () -> Unit
    ) {
        val title = activity.getString(R.string.auth)
        val subtitle = activity.getString(R.string.auth_by)

        val manager = BiometricManager.from(activity)
        val authenticators = if (Build.VERSION.SDK_INT > 29) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else BiometricManager.Authenticators.BIOMETRIC_STRONG

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)
            .build()

        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showErrorSnackbar("Biometric hardware is unavailable.")
                resultChannel.trySend(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showErrorSnackbar("Biometric feature is not available on this device.")
                resultChannel.trySend(BiometricResult.FeatureUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showErrorSnackbar("No biometric credentials are set up.")
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                return
            }
            else -> Unit
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showErrorSnackbar("Authentication error." + activity.getString(R.string.try_again))
                    resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showErrorSnackbar("Authentication failed." + activity.getString(R.string.try_again))
                    resultChannel.trySend(BiometricResult.AuthenticationFailed)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    resultChannel.trySend(BiometricResult.AuthenticationSuccess)
                    onSuccess()
                }
            }
        )
        prompt.authenticate(promptInfo)
    }

    private fun showErrorSnackbar(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true
            )
        }
    }

    sealed interface BiometricResult {
        data class AuthenticationError(val error: String): BiometricResult
        data object HardwareUnavailable: BiometricResult
        data object FeatureUnavailable: BiometricResult
        data object AuthenticationFailed: BiometricResult
        data object AuthenticationSuccess: BiometricResult
        data object AuthenticationNotSet: BiometricResult
    }
}
