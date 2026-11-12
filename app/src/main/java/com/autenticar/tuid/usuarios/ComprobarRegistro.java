package com.autenticar.tuid.usuarios;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.autenticar.tuid.R;
import com.autenticar.tuid.model.CheckRegistrationRequest;
import com.autenticar.tuid.model.CheckRegistrationResponse;
import com.autenticar.tuid.services.TuidServiceV2;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.CuilValidator;
import com.autenticar.tuid.v2.utils.LoadingDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Se muestra al tocar "Registrarse": antes de escanear el DNI pregunta el CUIL y consulta al servidor si
 * la persona ya esta registrada, para no repetir una registracion que no hace falta.
 *  - No registrada: sigue el registro habitual.
 *  - Registrada con Fe de Vida pendiente: ofrece ir directo a la Fe de Vida con el CUIL ya cargado.
 *  - Registrada y al dia / registro incompleto: lo informa.
 * Si no hay conexion NO se asume que no esta registrada: se avisa y se permite reintentar.
 */
public class ComprobarRegistro {

    public interface Destino {
        /** No esta registrada (o no sabe su CUIL): seguir con el registro. */
        void onContinuarRegistro();

        /** Ya esta registrada y le toca la Fe de Vida: ir directo con este CUIL. */
        void onRealizarFeDeVida(String cuil);
    }

    private final Activity activity;
    private final Destino destino;
    private AlertDialog dialog;

    public ComprobarRegistro(Activity activity, Destino destino) {
        this.activity = activity;
        this.destino = destino;
    }

    public void mostrar() {
        final View v = LayoutInflater.from(activity).inflate(R.layout.dialog_selector_usuario, null);
        ((TextView) v.findViewById(R.id.picker_titulo)).setText("Registrarse");
        ((TextView) v.findViewById(R.id.picker_mensaje)).setText(
                "Antes de comenzar, ingrese su CUIL para comprobar si ya está registrado en TuID Vida.");
        final EditText cuil = (EditText) v.findViewById(R.id.picker_cuil);
        cuil.setVisibility(View.VISIBLE);
        v.findViewById(R.id.picker_ayuda).setVisibility(View.VISIBLE);
        final TextView error = (TextView) v.findViewById(R.id.picker_error);

        dialog = new AlertDialog.Builder(activity, R.style.DialogoTuID).setView(v).setCancelable(true).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Button comprobar = (Button) v.findViewById(R.id.picker_principal);
        comprobar.setText("Comprobar");
        comprobar.setTextColor(Color.WHITE);
        comprobar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = CuilValidator.normalizar(cuil.getText().toString());
                String problema = CuilValidator.mensajeError(n);
                if (problema != null) {
                    error.setText(problema);
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                dialog.dismiss();
                consultar(n);
            }
        });
        Button noSe = (Button) v.findViewById(R.id.picker_secundario);
        noSe.setText("No sé mi CUIL");
        noSe.setBackgroundResource(R.drawable.btn_outline);
        noSe.setTextColor(activity.getResources().getColor(R.color.colorPrimaryDark));
        noSe.setVisibility(View.VISIBLE);
        noSe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                cuilEnRegistro = null;   // no sabe su CUIL: no se puede guardar en el telefono al terminar
                destino.onContinuarRegistro();
            }
        });
        v.findViewById(R.id.picker_cancelar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void consultar(final String cuil) {
        Audio.play(activity, R.raw.a05);
        LoadingDialog.getInstance().show(activity, "Aguarde un momento, comprobando su registro");
        ServiceGenerator.createV2Service(TuidServiceV2.class)
                .checkRegistration(new CheckRegistrationRequest(cuil))
                .enqueue(new Callback<CheckRegistrationResponse>() {
                    @Override
                    public void onResponse(Call<CheckRegistrationResponse> call, Response<CheckRegistrationResponse> r) {
                        LoadingDialog.getInstance().hide();
                        CheckRegistrationResponse body = r.body();
                        if (!r.isSuccessful() || body == null) {
                            sinRespuesta(cuil);
                            return;
                        }
                        if (!body.IsOk()) {
                            aviso("No pudimos comprobar su registro", body.Message != null ? body.Message
                                    : "Inténtelo nuevamente en unos minutos.", null, null);
                            return;
                        }
                        resultado(cuil, body);
                    }

                    @Override
                    public void onFailure(Call<CheckRegistrationResponse> call, Throwable t) {
                        LoadingDialog.getInstance().hide();
                        sinRespuesta(cuil);
                    }
                });
    }

    /** CUIL con el que la persona empieza su registro (si lo escribio); al terminar el registro se guarda en el telefono. */
    public static String cuilEnRegistro;

    private void resultado(final String cuil, CheckRegistrationResponse r) {
        String estado = r.registration == null ? "" : r.registration;
        if (CheckRegistrationResponse.NOT_REGISTERED.equals(estado)) {
            cuilEnRegistro = cuil;
            destino.onContinuarRegistro();
        } else if (CheckRegistrationResponse.PENDING.equals(estado)) {
            aviso("Fe de Vida pendiente",
                    "Usted ya se encuentra registrado en TuID Vida.\n\nDebe realizar su Fe de Vida para mantener actualizada su certificación.",
                    "Realizar Fe de Vida", new Runnable() {
                        @Override
                        public void run() {
                            destino.onRealizarFeDeVida(cuil);
                        }
                    });
        } else if (CheckRegistrationResponse.UP_TO_DATE.equals(estado)) {
            aviso("Usuario registrado",
                    "Usted ya se encuentra registrado en TuID Vida.\n\n" + AlDia.mensaje(r.proximas),
                    null, null);
        } else if (CheckRegistrationResponse.NOT_ADHERED.equals(estado)) {
            // El CUIL no figura en usuario_empresa: no pertenece a una entidad adherida, no se puede registrar
            Cuadro.mostrar(activity, "Entidad no adherida",
                    r.Message != null ? r.Message : "Su CUIL no figura en ninguna entidad adherida a TuID Vida.",
                    "Ver entidades adheridas", new Runnable() {
                        @Override
                        public void run() {
                            activity.startActivity(new android.content.Intent(activity, com.autenticar.tuid.v2.EntidadesAdheridasActivity.class));
                        }
                    }, "Volver");
        } else if (CheckRegistrationResponse.INCOMPLETE.equals(estado)) {
            Soporte.mostrarRegistroIncompleto(activity, cuil);
        } else {
            aviso("No pudimos comprobar su registro", "Inténtelo nuevamente en unos minutos.", null, null);
        }
    }

    private void sinRespuesta(final String cuil) {
        Cuadro.mostrar(activity, "Sin conexión",
                "No pudimos comprobar su registro. Verifique su conexión a Internet e inténtelo nuevamente.",
                "Reintentar", new Runnable() {
                    @Override
                    public void run() {
                        consultar(cuil);
                    }
                }, "Cancelar");
    }

    private void aviso(String titulo, String mensaje, String botonAccion, Runnable accion) {
        if (botonAccion != null) {
            Cuadro.mostrar(activity, titulo, mensaje, botonAccion, accion, "Volver");
        } else {
            Cuadro.mostrar(activity, titulo, mensaje, "Volver al menú", null, null);
        }
    }
}
