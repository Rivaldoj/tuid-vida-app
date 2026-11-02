package com.autenticar.tuid.v2.nfc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SOLO DEBUG. Pantalla de prueba para leer el chip NFC de un pasaporte o del nuevo DNI.
 * No envia nada a ningun servidor ni escribe datos en el log: el resultado queda solo en pantalla.
 */
public class NfcTestActivity extends Activity implements NfcAdapter.ReaderCallback {

    private EditText etDoc, etBirth, etExpiry, etCan;
    private TextView tvOut;
    private ImageView ivFoto;
    private static final int REQ_MRZ = 3001;
    // leyendo: hay una lectura en curso. conExito: hay un resultado correcto en pantalla (se conserva
    // hasta que el usuario pida una nueva lectura; mover o alejar el documento ya no lo borra).
    private volatile boolean leyendo = false;
    private volatile boolean conExito = false;
    private NfcAdapter nfc;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(p, p * 2, p, p);

        TextView t = new TextView(this);
        t.setText("Prueba NFC (solo debug)\n"
                + "Tocá \"Escanear MRZ con la cámara\" (o cargá los datos a mano) y después apoyá el documento en la parte trasera del teléfono.\n"
                + "Fechas en formato AAMMDD. El código CAN (6 dígitos) es opcional: si lo dejás vacío se usa la MRZ.");
        root.addView(t);

        android.widget.Button btnScan = new android.widget.Button(this);
        btnScan.setText("Escanear MRZ con la cámara");
        btnScan.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivityForResult(new android.content.Intent(NfcTestActivity.this, MrzScanActivity.class), REQ_MRZ);
            }
        });
        root.addView(btnScan);
        android.widget.Button btnNueva = new android.widget.Button(this);
        btnNueva.setText("Nueva lectura (borrar datos)");
        btnNueva.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                conExito = false;
                limpiarResultado();
                tvOut.setText("Esperando documento…");
            }
        });
        root.addView(btnNueva);

        etDoc = campo(root, "N° de documento (como figura en la MRZ)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        etBirth = campo(root, "Fecha de nacimiento AAMMDD", InputType.TYPE_CLASS_NUMBER);
        etExpiry = campo(root, "Fecha de vencimiento AAMMDD", InputType.TYPE_CLASS_NUMBER);
        etCan = campo(root, "CAN de 6 dígitos (opcional)", InputType.TYPE_CLASS_NUMBER);

        tvOut = new TextView(this);
        tvOut.setPadding(0, p, 0, p);
        tvOut.setText("Esperando documento…");
        root.addView(tvOut);
        ivFoto = new ImageView(this);
        ivFoto.setAdjustViewBounds(true);
        ivFoto.setLayoutParams(new LinearLayout.LayoutParams(p * 10, p * 12, Gravity.START));
        root.addView(ivFoto);

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
        nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc == null) tvOut.setText("Este teléfono no tiene NFC.");
        else if (!nfc.isEnabled()) tvOut.setText("El NFC está apagado: activalo en Ajustes.");
    }

    @Override
    protected void onActivityResult(int req, int res, android.content.Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_MRZ && res == RESULT_OK && data != null) {
            etDoc.setText(data.getStringExtra(MrzScanActivity.EXTRA_DOC));
            etBirth.setText(data.getStringExtra(MrzScanActivity.EXTRA_BIRTH));
            etExpiry.setText(data.getStringExtra(MrzScanActivity.EXTRA_EXPIRY));
            conExito = false;
            limpiarResultado();
            tvOut.setText("MRZ leída y validada. Ahora apoyá el documento contra la parte trasera del teléfono.");
        }
    }

    private Bitmap fotoActual;

    /** Borra el resultado, la foto y los datos de la lectura anterior (nunca mostrar datos de otro documento). */
    private void limpiarResultado() {
        ivFoto.setImageBitmap(null);
        if (fotoActual != null && !fotoActual.isRecycled()) fotoActual.recycle();
        fotoActual = null;
        tvOut.setText("");
    }

    private EditText campo(LinearLayout parent, String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(type);
        e.setSingleLine(true);
        parent.addView(e);
        return e;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // al volver a la pantalla no debe quedar nada de la lectura anterior
        if (ivFoto != null && tvOut != null && fotoActual != null) { conExito = false; limpiarResultado(); }
        if (nfc != null) {
            nfc.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfc != null) nfc.disableReaderMode(this);
    }

    @Override
    public void onTagDiscovered(final Tag tag) {
        // Ignorar si ya hay una lectura en curso o un resultado correcto en pantalla
        if (leyendo || conExito) return;
        final String doc = etDoc.getText().toString().trim().toUpperCase();
        final String birth = etBirth.getText().toString().trim();
        final String expiry = etExpiry.getText().toString().trim();
        final String can = etCan.getText().toString().trim();
        final IsoDep iso = IsoDep.get(tag);
        if (iso == null) {
            mostrar("La tarjeta detectada no es compatible (no es IsoDep).", null);
            return;
        }
        if (can.isEmpty() && (doc.isEmpty() || birth.length() != 6 || expiry.length() != 6)) {
            mostrar("Faltan datos: cargá número de documento, nacimiento y vencimiento (AAMMDD), o el CAN.", null);
            return;
        }
        runOnUiThread(new Runnable() { @Override public void run() { limpiarResultado(); } });
        mostrar("Leyendo… no muevas el documento.\nClave usada: "
                + (can.isEmpty() ? "MRZ (doc " + doc + ", nac " + birth + ", venc " + expiry + ")" : "CAN"), null);
        leyendo = true;
        io.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EmrtdReader.Result r = null;
                    // Si se corta el contacto NFC (Tag lost) se vuelve a conectar y se reintenta, hasta 3 veces
                    for (int intento = 1; intento <= 3; intento++) {
                        try {
                            if (intento > 1) {
                                mostrar("Se cortó el contacto. Reintentando (" + intento + "/3)… no muevas el documento.", null);
                                Thread.sleep(400);
                                leyendo = false;
                    try { iso.close(); } catch (Exception ignored) { }
                                iso.connect();
                            }
                            r = new EmrtdReader(getApplicationContext()).read(iso, can, doc, birth, expiry);
                            break;
                        } catch (EmrtdReader.EmrtdException ex) {
                            if (intento == 3 || !esCorteDeContacto(ex)) throw ex;
                        }
                    }
                    StringBuilder s = new StringBuilder("LECTURA OK (" + r.protocolo + ")\n");
                    s.append("Tipo: ").append(r.tipoDocumento).append('\n');
                    s.append("País emisor: ").append(r.paisEmisor).append('\n');
                    s.append("Apellidos: ").append(r.apellidos).append('\n');
                    s.append("Nombres: ").append(r.nombres).append('\n');
                    s.append("N° documento: ").append(r.numeroDocumento).append('\n');
                    s.append("Nacionalidad: ").append(r.nacionalidad).append('\n');
                    s.append("Nacimiento: ").append(r.fechaNacimiento).append('\n');
                    s.append("Sexo: ").append(r.sexo).append('\n');
                    s.append("Vencimiento: ").append(r.fechaVencimiento).append('\n');
                    s.append("Foto: ").append(r.fotoMime).append(' ').append(r.fotoAncho).append('x')
                            .append(r.fotoAlto).append(", ").append(r.fotoBytes).append(" bytes\n");
                    if (r.autenticidad != null) {
                        PassiveAuth.Info a = r.autenticidad;
                        s.append("\nAUTENTICIDAD DEL CHIP: ").append(a.autentico() ? "VERIFICADA" : "NO VERIFICADA").append('\n');
                        s.append("  Datos sin alterar: ").append(a.hashesOk ? "si" : "NO").append('\n');
                        s.append("  Firma del emisor: ").append(a.firmaOk ? "valida" : "NO valida").append('\n');
                        s.append("  Cadena hasta el pais: ").append(a.cadenaOk ? "valida" : "NO verificada").append('\n');
                        s.append(a.detalle).append('\n');
                    }
                    Bitmap bmp = null;
                    if (r.foto != null) {
                        try {
                            if (com.gemalto.jp2.JP2Decoder.isJPEG2000(r.foto)) {
                                bmp = new com.gemalto.jp2.JP2Decoder(r.foto).decode();
                            } else {
                                bmp = BitmapFactory.decodeByteArray(r.foto, 0, r.foto.length);
                            }
                        } catch (Throwable e) {
                            s.append("(no se pudo decodificar la foto: ").append(e.getClass().getSimpleName()).append(")\n");
                        }
                        if (bmp != null) {
                            // Lo que se enviaria al backend (idDocument.photo): JPEG normal
                            java.io.ByteArrayOutputStream jpg = new java.io.ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 85, jpg);
                            s.append("Foto convertida a JPEG: ").append(jpg.size()).append(" bytes\n");
                        }
                    }
                    conExito = true;
                    mostrar(s.toString(), bmp);
                } catch (Exception e) {
                    mostrar(describirError(e), null);
                } finally {
                    try { iso.close(); } catch (Exception ignored) { }
                }
            }
        });
    }

    /** true si el fallo fue una perdida de contacto con el chip (el documento se movio o se alejo). */
    private static boolean esCorteDeContacto(Throwable e) {
        for (Throwable c = e; c != null; c = c.getCause()) {
            if (c instanceof android.nfc.TagLostException) return true;
            String m = c.getMessage();
            if (m != null && (m.contains("Tag was lost") || m.contains("Transceive failed")
                    || m.contains("Could not tranceive"))) return true;
        }
        return false;
    }

    /** Etapa + cadena de causas + primeras lineas de la pila (sin datos personales). */
    private String describirError(Exception e) {
        StringBuilder s = new StringBuilder();
        Throwable raiz = e;
        if (e instanceof EmrtdReader.EmrtdException) {
            EmrtdReader.EmrtdException x = (EmrtdReader.EmrtdException) e;
            s.append("ERROR en la etapa: ").append(x.etapa).append('\n');
            s.append("PACE: ").append(x.detallePace).append('\n');
            raiz = x.getCause() != null ? x.getCause() : e;
        }
        int n = 0;
        for (Throwable t = raiz; t != null && n < 5; t = t.getCause(), n++) {
            s.append(n == 0 ? "Causa: " : "  causada por: ").append(t.getClass().getName());
            if (t.getMessage() != null) s.append(" - ").append(t.getMessage());
            s.append('\n');
        }
        int frames = 0;
        for (StackTraceElement f : raiz.getStackTrace()) {
            String c = f.getClassName();
            if (c.startsWith("org.jmrtd") || c.startsWith("net.sf.scuba") || c.startsWith("android.nfc")) {
                s.append("   en ").append(c.substring(c.lastIndexOf('.') + 1)).append('.').append(f.getMethodName())
                        .append(':').append(f.getLineNumber()).append('\n');
                if (++frames >= 6) break;
            }
        }
        s.append("\nSi dice 'tag lost' / 'Transceive failed': apoyá el documento firme y quieto.");
        return s.toString();
    }

    private void mostrar(final String texto, final Bitmap foto) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fotoActual != null && fotoActual != foto && !fotoActual.isRecycled()) fotoActual.recycle();
                fotoActual = foto;
                tvOut.setText(texto);
                ivFoto.setImageBitmap(foto);
            }
        });
    }
}
