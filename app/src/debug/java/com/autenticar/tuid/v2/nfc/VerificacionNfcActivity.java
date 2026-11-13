package com.autenticar.tuid.v2.nfc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.autenticar.tuid.R;
import com.autenticar.tuid.usuarios.Audio;
import com.autenticar.tuid.utils.EdgeInsets;
import com.autenticar.tuid.utils.HelperFunctions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SOLO DEBUG. Pantalla 3 del registro con DNI nuevo: instrucciones, lectura del chip y resultado.
 * Usa las claves cargadas en la pantalla 2 (NfcClaves). No envia nada al servidor ni escribe datos personales en el log.
 */
public class VerificacionNfcActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private static final int MAX_INTENTOS = 3;

    private View vistaInstrucciones;
    private View vistaLectura;
    private ImageView icono;
    private ImageView foto;
    private TextView titulo;
    private TextView texto;
    private Button boton;

    private NfcAdapter nfc;
    private boolean leyendoModo = false;      // el lector NFC esta activo
    private volatile boolean leyendo = false; // hay una lectura en curso
    private volatile boolean terminado = false;
    private int fallos = 0;
    private Bitmap fotoActual;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_verificacion_nfc);
        EdgeInsets.aplicar(findViewById(R.id.raiz));
        vistaInstrucciones = findViewById(R.id.vista_instrucciones);
        vistaLectura = findViewById(R.id.vista_lectura);
        icono = findViewById(R.id.lectura_icono);
        foto = findViewById(R.id.lectura_foto);
        titulo = findViewById(R.id.lectura_titulo);
        texto = findViewById(R.id.lectura_texto);
        boton = findViewById(R.id.btn_continuar);
        nfc = NfcAdapter.getDefaultAdapter(this);

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accionBoton();
            }
        });
        Audio.play(this, R.raw.a28_3);
    }

    private enum Estado { INSTRUCCIONES, SIN_NFC, NFC_APAGADO, LEYENDO, FALLO, EXITO }

    private Estado estado = Estado.INSTRUCCIONES;

    private void accionBoton() {
        switch (estado) {
            case INSTRUCCIONES:
            case NFC_APAGADO:
            case FALLO:
                comenzarLectura();
                break;
            case SIN_NFC:
                terminarYSalir();
                break;
            case EXITO:
                continuarRegistro();
                break;
            default:
                break;
        }
    }

    private void comenzarLectura() {
        vistaInstrucciones.setVisibility(View.GONE);
        vistaLectura.setVisibility(View.VISIBLE);
        if (nfc == null) {
            estado = Estado.SIN_NFC;
            titulo.setText("Este celular no tiene NFC");
            texto.setText("Este celular no puede leer el chip del DNI nuevo. Puede usar otro celular o registrarse con ayuda de Soporte.");
            boton.setText("Salir");
            return;
        }
        if (!nfc.isEnabled()) {
            estado = Estado.NFC_APAGADO;
            titulo.setText("Active el NFC");
            texto.setText("El NFC está apagado. Toque el botón, active el NFC y vuelva a esta pantalla.");
            boton.setText("Abrir ajustes");
            boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                    boton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v2) {
                            accionBoton();
                        }
                    });
                    boton.setText("Continuar");
                }
            });
            return;
        }
        estado = Estado.LEYENDO;
        titulo.setText("Apoye el DNI");
        texto.setText("Apoye el DNI en la parte de atrás del celular, quieto y bien pegado.");
        boton.setVisibility(View.GONE);
        activarLector();
    }

    private void activarLector() {
        if (nfc != null && nfc.isEnabled() && !leyendoModo) {
            nfc.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
            leyendoModo = true;
        }
    }

    private void desactivarLector() {
        if (nfc != null && leyendoModo) {
            nfc.disableReaderMode(this);
            leyendoModo = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (estado == Estado.LEYENDO || estado == Estado.FALLO) activarLector();
    }

    @Override
    protected void onPause() {
        super.onPause();
        desactivarLector();
    }

    @Override
    public void onTagDiscovered(final Tag tag) {
        if (leyendo || terminado) return;
        final IsoDep iso = IsoDep.get(tag);
        if (iso == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mostrarFallo("La tarjeta detectada no es un DNI con chip. Pruebe con el DNI nuevo.");
                }
            });
            return;
        }
        final String can = NfcClaves.can;
        final String doc = NfcClaves.doc;
        final String nac = NfcClaves.nacimiento;
        final String ven = NfcClaves.vencimiento;
        leyendo = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                titulo.setText("Leyendo…");
                texto.setText("No mueva el DNI. La lectura tarda entre dos y cinco segundos.");
                Audio.play(VerificacionNfcActivity.this, R.raw.a28_3);
            }
        });
        io.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EmrtdReader.Result r = null;
                    for (int intento = 1; intento <= 3; intento++) {
                        try {
                            if (intento > 1) {
                                Thread.sleep(400);
                                try { iso.close(); } catch (Exception ignored) { }
                                iso.connect();
                            }
                            r = new EmrtdReader(getApplicationContext()).read(iso, can, doc, nac, ven);
                            break;
                        } catch (EmrtdReader.EmrtdException ex) {
                            if (intento == 3 || !esCorteDeContacto(ex)) throw ex;
                        }
                    }
                    Bitmap bmp = decodificarFoto(r);
                    mostrarExito(r, bmp);
                } catch (final Exception e) {
                    final boolean conCan = !can.isEmpty();
                    final boolean contacto = esCorteDeContacto(e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fallos++;
                            if (!contacto && conCan) {
                                mostrarFallo("El código CAN no es correcto o el chip no aceptó la clave. Revíselo e inténtelo de nuevo.");
                            } else if (fallos >= MAX_INTENTOS) {
                                mostrarFallo("No pudimos leer su DNI. Puede intentarlo otra vez o registrarse con ayuda de Soporte.");
                            } else {
                                mostrarFallo("No pudimos leer el chip. Apoye el DNI de nuevo, quieto y bien pegado al celular.");
                            }
                        }
                    });
                } finally {
                    leyendo = false;
                    try { iso.close(); } catch (Exception ignored) { }
                }
            }
        });
    }

    private Bitmap decodificarFoto(EmrtdReader.Result r) {
        if (r == null || r.foto == null) return null;
        try {
            if (com.gemalto.jp2.JP2Decoder.isJPEG2000(r.foto)) return new com.gemalto.jp2.JP2Decoder(r.foto).decode();
            return BitmapFactory.decodeByteArray(r.foto, 0, r.foto.length);
        } catch (Throwable t) {
            return null;
        }
    }

    private void mostrarExito(final EmrtdReader.Result r, final Bitmap bmp) {
        terminado = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                desactivarLector();
                estado = Estado.EXITO;
                HelperFunctions.Vibrate(VerificacionNfcActivity.this);
                titulo.setText("Listo");
                StringBuilder s = new StringBuilder("Leímos y validamos su DNI.\n\n");
                s.append(r.nombres).append(' ').append(r.apellidos).append('\n');
                s.append("DNI ").append(r.numeroDocumento).append('\n');
                if (r.autenticidad != null) {
                    s.append(r.autenticidad.autentico() ? "Chip auténtico (firma del emisor válida)" : "Chip NO verificado");
                }
                s.append("\nClave usada: ").append(r.protocolo);
                texto.setText(s.toString());
                if (bmp != null) {
                    fotoActual = bmp;
                    foto.setImageBitmap(bmp);
                    foto.setVisibility(View.VISIBLE);
                }
                resultado = r;
                boton.setVisibility(View.VISIBLE);
                boton.setText("Continuar");
            }
        });
    }

    private EmrtdReader.Result resultado;

    private static String limpio(String s) {
        return s == null ? "" : s.replace('<', ' ').trim();
    }

    /** yyMMdd del chip a yyyy-MM-dd. En nacimientos, un anio mayor al actual es del siglo pasado. */
    private static String fecha(String yyMMdd, boolean nacimiento) {
        if (yyMMdd == null || yyMMdd.length() != 6) return null;
        int yy = Integer.parseInt(yyMMdd.substring(0, 2));
        int actual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100;
        int siglo = (nacimiento && yy > actual) ? 1900 : 2000;
        return String.format(java.util.Locale.US, "%04d-%s-%s", siglo + yy, yyMMdd.substring(2, 4), yyMMdd.substring(4, 6));
    }

    private static String sexoServidor(String sexo) {
        String s = sexo == null ? "" : sexo.trim().toUpperCase();
        if (s.startsWith("M")) return "M";
        if (s.startsWith("F")) return "F";
        return "X";
    }

    /**
     * Sigue con el registro: prueba de vida de AWS, comparacion con la foto del chip y, despues, correo y telefono.
     * Los datos pasan en memoria (RegistroNfcDatos); no se fotografian el frente ni el dorso del DNI.
     */
    private void continuarRegistro() {
        if (resultado == null || fotoActual == null) {
            boton.setText("Salir");
            estado = Estado.SIN_NFC;
            titulo.setText("No se pudo leer la foto");
            texto.setText("El chip no entregó la foto del rostro. No podemos continuar con el registro.");
            return;
        }
        Bitmap f = fotoActual;
        if (f.getWidth() > 800) {
            f = Bitmap.createScaledBitmap(f, 800, Math.max(1, f.getHeight() * 800 / f.getWidth()), true);
        }
        java.io.ByteArrayOutputStream jpg = new java.io.ByteArrayOutputStream();
        f.compress(Bitmap.CompressFormat.JPEG, 90, jpg);

        com.autenticar.tuid.v2.RegistroNfcDatos.borrar();
        com.autenticar.tuid.v2.RegistroNfcDatos.activo = true;
        com.autenticar.tuid.v2.RegistroNfcDatos.numero = limpio(resultado.numeroDocumento).replace(" ", "");
        com.autenticar.tuid.v2.RegistroNfcDatos.nombre = limpio(resultado.nombres);
        com.autenticar.tuid.v2.RegistroNfcDatos.apellido = limpio(resultado.apellidos);
        com.autenticar.tuid.v2.RegistroNfcDatos.sexo = sexoServidor(resultado.sexo);
        com.autenticar.tuid.v2.RegistroNfcDatos.nacimiento = fecha(resultado.fechaNacimiento, true);
        com.autenticar.tuid.v2.RegistroNfcDatos.vencimiento = fecha(resultado.fechaVencimiento, false);
        com.autenticar.tuid.v2.RegistroNfcDatos.tramite = resultado.tramite;
        String nac = limpio(resultado.nacionalidad).toUpperCase();
        com.autenticar.tuid.v2.RegistroNfcDatos.pais = nac.isEmpty() ? null : nac;
        com.autenticar.tuid.v2.RegistroNfcDatos.fotoBase64 = android.util.Base64.encodeToString(jpg.toByteArray(), android.util.Base64.NO_WRAP);
        if (!NfcClaves.can.isEmpty()) com.autenticar.tuid.v2.RegistroNfcDatos.can = NfcClaves.can;
        else com.autenticar.tuid.v2.RegistroNfcDatos.mrz = resultado.mrz;

        // Inicio debajo (cierra Terminos, Claves y Verificacion) y encima la pantalla de la prueba de vida del registro
        Intent inicio = new Intent(this, com.autenticar.tuid.v2.HomeActivity.class);
        inicio.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Intent prueba = new Intent(this, com.autenticar.tuid.v2.PreGestureActivity.class);
        NfcClaves.borrar();
        startActivities(new Intent[]{inicio, prueba});
        finish();
    }

    private void mostrarFallo(String mensaje) {
        estado = Estado.FALLO;
        titulo.setText("No se pudo leer");
        texto.setText(mensaje);
        boton.setVisibility(View.VISIBLE);
        boton.setText("Reintentar");
    }

    private static boolean esCorteDeContacto(Throwable e) {
        for (Throwable c = e; c != null; c = c.getCause()) {
            if (c instanceof android.nfc.TagLostException) return true;
            String m = c.getMessage();
            if (m != null && (m.contains("Tag was lost") || m.contains("Transceive failed")
                    || m.contains("Could not tranceive"))) return true;
        }
        return false;
    }

    private void terminarYSalir() {
        NfcClaves.borrar();
        if (fotoActual != null) foto.setImageBitmap(null);
        fotoActual = null;
        // Vuelve al Inicio y cierra las pantallas de Terminos, Claves y Verificacion que quedaron debajo
        Intent inicio = new Intent(this, com.autenticar.tuid.v2.HomeActivity.class);
        inicio.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(inicio);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Audio.detener();
        NfcClaves.borrar();
    }
}
