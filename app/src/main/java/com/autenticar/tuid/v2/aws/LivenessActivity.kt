package com.autenticar.tuid.v2.aws

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.autenticar.tuid.R
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
            MaterialTheme(colorScheme = colores(), typography = tipografia()) {
                // systemBarsPadding: deja libres la barra de estado y la de navegacion del telefono
                // (sin esto el boton queda tapado por los botones del sistema)
                var iniciado by remember { mutableStateOf(!USAR_PANTALLA_PROPIA) }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (iniciado) MaterialTheme.colorScheme.background else Color(0xFF01ACC2))
                        .systemBarsPadding()
                ) {
                    if (!iniciado) {
                        PantallaInicio(onComenzar = { iniciado = true })
                    } else {
                        FaceLivenessDetector(
                            sessionId = sessionId,
                            region = region,
                            disableStartView = USAR_PANTALLA_PROPIA,
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
        }
    }

    /** Colores de TuID (turquesa #01acc2) sobre el esquema base de la libreria. */
    @Composable
    private fun colores() = LivenessColorScheme.default().copy(
        primary = Color(0xFF01ACC2),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD9F1F5),
        onPrimaryContainer = Color(0xFF1D2A2C),
        secondary = Color(0xFF018899),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD9F1F5),
        onSecondaryContainer = Color(0xFF1D2A2C),
        tertiary = Color(0xFF018899),
        error = Color(0xFFE63936),
        onError = Color.White,
        // el aviso de fotosensibilidad usa estos dos colores: fondo amarillo claro y letra oscura
        errorContainer = Color(0xFFFFF4B8),
        onErrorContainer = Color(0xFF1D2A2C)
    )

    /** Titulos con Oswald (la letra de TuID); el resto conserva la tipografia base. */
    private fun tipografia(): Typography {
        val oswald = FontFamily(Font(R.font.oswald_variable))
        val base = Typography()
        return base.copy(
            headlineLarge = base.headlineLarge.copy(fontFamily = oswald),
            headlineMedium = base.headlineMedium.copy(fontFamily = oswald),
            headlineSmall = base.headlineSmall.copy(fontFamily = oswald),
            titleLarge = base.titleLarge.copy(fontFamily = oswald),
            titleMedium = base.titleMedium.copy(fontFamily = oswald),
            labelLarge = base.labelLarge.copy(fontFamily = oswald)
        )
    }

    /**
     * Pantalla de inicio propia (estilo TuID): aviso de fotosensibilidad con el icono de atencion y un boton grande.
     * La vista de inicio de la libreria no permite cambiar el icono ni el boton, por eso se la omite.
     */
    @Composable
    private fun PantallaInicio(onComenzar: () -> Unit) {
        val oswald = FontFamily(Font(R.font.oswald_variable))
        val tinta = Color(0xFF1D2A2C)
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "PRUEBA DE VIDA",
                color = Color.White,
                fontFamily = oswald,
                fontSize = 30.sp,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 18.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(horizontal = 18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 22.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF4B8), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_atencion),
                            contentDescription = "Atención",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Aviso de fotosensibilidad",
                                color = tinta,
                                fontFamily = oswald,
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Esta prueba muestra destellos de colores. Tenga precaución si es sensible a las luces.",
                                color = tinta,
                                fontSize = 17.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Ubique su rostro en el centro del óvalo y siga las indicaciones de la pantalla.",
                        color = tinta,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onComenzar,
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF01ACC2),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .shadow(3.dp, RoundedCornerShape(40.dp))
                ) {
                    Text(
                        text = "COMENZAR LA PRUEBA",
                        fontFamily = oswald,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    private fun finishWithError(message: String) {
        setResult(RESULT_CANCELED, Intent().putExtra(EXTRA_ERROR, message))
        finish()
    }

    companion object {
        private const val TAG = "LivenessActivity"

        /** true: pantalla de inicio propia (icono y boton de TuID); false: la vista de inicio de la libreria. */
        private const val USAR_PANTALLA_PROPIA = true
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
