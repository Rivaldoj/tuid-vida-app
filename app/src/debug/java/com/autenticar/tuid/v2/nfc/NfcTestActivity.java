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
                + "Cargá los datos de la zona MRZ del documento y apoyalo en la parte trasera del teléfono.\n"
                + "Fechas en formato AAMMDD. El código CAN (6 dígitos) es opcional: si lo dejás vacío se usa la MRZ.");
        root.addView(t);

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
        mostrar("Leyendo… no muevas el documento.", null);
        io.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EmrtdReader.Result r = new EmrtdReader().read(iso, can, doc, birth, expiry);
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
                    Bitmap bmp = null;
                    if (r.foto != null) {
                        bmp = BitmapFactory.decodeByteArray(r.foto, 0, r.foto.length);
                        if (bmp == null) s.append("(la foto es JPEG 2000: falta el decodificador para mostrarla)\n");
                    }
                    mostrar(s.toString(), bmp);
                } catch (Exception e) {
                    mostrar("ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage()
                            + "\nRevisá los datos de la MRZ y volvé a apoyar el documento.", null);
                } finally {
                    try { iso.close(); } catch (Exception ignored) { }
                }
            }
        });
    }

    private void mostrar(final String texto, final Bitmap foto) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvOut.setText(texto);
                ivFoto.setImageBitmap(foto);
            }
        });
    }
}
