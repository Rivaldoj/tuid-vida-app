package com.autenticar.tuid.v2.aws;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.autenticar.tuid.BuildConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SOLO DEBUG. Prueba de AWS Face Liveness de punta a punta en el dispositivo:
 *  1) GET {VIDA_BASE_URL}/session con el token de la marca blanca -> sessionId
 *  2) abre LivenessActivity (componente de Amplify) con ese sessionId
 *  3) muestra como termino la prueba en el dispositivo.
 * El resultado (aprobado o no) lo decide siempre el backend; aqui solo se ve la parte del dispositivo.
 * El token se lee de vida.properties (no versionado). No se imprime ni se registra.
 */
public class LivenessTestActivity extends Activity {

    private static final int REQ_LIVENESS = 4001;
    private static final int REQ_CAMERA = 4002;

    private TextView tv;
    private Button btn;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(p, p * 2, p, p);

        TextView t = new TextView(this);
        t.setText("Prueba de AWS Face Liveness (solo debug)\n"
                + "Pide una sesión al backend y abre la prueba de vida de Amazon Rekognition.\n"
                + "Servidor: " + BuildConfig.VIDA_BASE_URL);
        root.addView(t);

        btn = new Button(this);
        btn.setText("Probar Face Liveness");
        btn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                empezar();
            }
        });
        root.addView(btn);

        tv = new TextView(this);
        tv.setPadding(0, p, 0, 0);
        root.addView(tv);
        if (BuildConfig.VIDA_TOKEN.isEmpty()) {
            tv.setText("Falta el token de la marca blanca.\nCreá el archivo vida.properties en la raíz del proyecto "
                    + "(ver vida.properties.example) y volvé a compilar.");
        }
        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }

    private void empezar() {
        if (BuildConfig.VIDA_TOKEN.isEmpty()) {
            tv.setText("Falta el token de la marca blanca (vida.properties).");
            return;
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            return;
        }
        btn.setEnabled(false);
        tv.setText("1/3 Pidiendo la sesión al servidor…");
        io.execute(new Runnable() {
            @Override
            public void run() {
                String sessionId = null;
                String error = null;
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL(BuildConfig.VIDA_BASE_URL + "/session").openConnection();
                    c.setRequestMethod("GET");
                    c.setRequestProperty("Authorization", BuildConfig.VIDA_TOKEN);
                    c.setConnectTimeout(15000);
                    c.setReadTimeout(30000);
                    int code = c.getResponseCode();
                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            code >= 400 ? c.getErrorStream() : c.getInputStream()));
                    StringBuilder s = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) s.append(line);
                    r.close();
                    if (code == 401) {
                        error = "El servidor rechazó el token (401). Revisá vida.properties.";
                    } else {
                        JSONObject j = new JSONObject(s.toString());
                        if (j.has("sessionId")) sessionId = j.getString("sessionId");
                        else error = "El servidor no creó la sesión: " + j.optString("message", "sin detalle")
                                + (j.has("details") ? " (" + j.optString("details") + ")" : "");
                    }
                } catch (Exception e) {
                    error = "No se pudo contactar al servidor: " + e.getClass().getSimpleName();
                }
                final String sid = sessionId;
                final String err = error;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (sid == null) {
                            tv.setText(err);
                            btn.setEnabled(true);
                            return;
                        }
                        tv.setText("2/3 Sesión creada (" + sid.substring(0, Math.min(8, sid.length())) + "…). Abriendo la prueba de vida…");
                        startActivityForResult(LivenessActivity.intent(LivenessTestActivity.this, sid,
                                getString(com.autenticar.tuid.R.string.aws_liveness_region)), REQ_LIVENESS);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        btn.setEnabled(true);
        if (req != REQ_LIVENESS) return;
        if (res == RESULT_OK) {
            tv.setText("3/3 La prueba de vida terminó en el dispositivo.\n"
                    + "El resultado (aprobado o no) lo decide el backend; esta pantalla no lo consulta.");
        } else {
            String e = data != null ? data.getStringExtra(LivenessActivity.EXTRA_ERROR) : null;
            tv.setText("La prueba de vida no se completó" + (e != null ? ": " + e : "") + ".");
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == REQ_CAMERA && res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) empezar();
        else if (code == REQ_CAMERA) tv.setText("Sin permiso de cámara: la prueba de vida lo necesita.");
    }
}
