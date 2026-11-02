package com.autenticar.tuid.v2.aws

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.ui.liveness.ui.FaceLivenessDetector
import com.amplifyframework.ui.liveness.ui.LivenessColorScheme

/**
 * Pantalla de prueba de vida con AWS Rekognition Face Liveness.
 *
 * Flujo:
 *  1. La app pide a nuestro backend que cree la sesion (CreateFaceLivenessSession) y recibe
 *     sessionId + region.
 *  2. Se abre esta Activity con esos datos. El componente de Amplify captura el video y lo
 *     transmite a Rekognition.
 *  3. Al terminar (RESULT_OK) la app pide el resultado al backend (GetFaceLivenessSessionResults).
 *     Este componente NO devuelve el resultado: lo decide siempre el servidor.
 *
 * Uso desde Java:
 *   startActivityForResult(LivenessActivity.intent(this, sessionId, region), REQUEST_LIVENESS);
 */
class LivenessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        val region = intent.getStringExtra(EXTRA_REGION)
        if (sessionId.isNullOrBlank() || region.isNullOrBlank()) {
            finishWithError("Faltan sessionId o region")
            return
        }
        if (!ensureAmplify(applicationContext)) {
            finishWithError("AWS no esta configurado (res/raw/amplifyconfiguration.json)")
            return
        }

        setContent {
            MaterialTheme(colorScheme = LivenessColorScheme.default()) {
                FaceLivenessDetector(
                    sessionId = sessionId,
                    region = region,
                    onComplete = {
                        setResult(RESULT_OK)
                        finish()
                    },
                    onError = { error ->
                        Log.e(TAG, "Liveness error: ${error.javaClass.simpleName} - $error")
                        finishWithError(error.javaClass.simpleName)
                    }
                )
            }
        }
    }

    private fun finishWithError(message: String) {
        setResult(RESULT_CANCELED, Intent().putExtra(EXTRA_ERROR, message))
        finish()
    }

    companion object {
        private const val TAG = "LivenessActivity"
        const val EXTRA_SESSION_ID = "SESSION_ID"
        const val EXTRA_REGION = "REGION"
        const val EXTRA_ERROR = "ERROR"

        @JvmStatic
        fun intent(context: Context, sessionId: String, region: String): Intent =
            Intent(context, LivenessActivity::class.java)
                .putExtra(EXTRA_SESSION_ID, sessionId)
                .putExtra(EXTRA_REGION, region)

        @Volatile
        private var amplifyReady = false

        /** Configura Amplify (Cognito Identity Pool) una sola vez. */
        @JvmStatic
        @Synchronized
        fun ensureAmplify(context: Context): Boolean {
            if (amplifyReady) return true
            return try {
                Amplify.addPlugin(AWSCognitoAuthPlugin())
                Amplify.configure(context)
                amplifyReady = true
                true
            } catch (e: AmplifyException) {
                // AlreadyConfiguredException cuenta como listo; cualquier otra cosa, no.
                amplifyReady = e.javaClass.simpleName == "AlreadyConfiguredException"
                if (!amplifyReady) Log.e(TAG, "No se pudo configurar Amplify", e)
                amplifyReady
            }
        }
    }
}
