package com.autenticar.tuid.v2;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.autenticar.tuid.AmplitudeManager.AmplitudeManager;
import com.autenticar.tuid.HistoryActivity;
import com.autenticar.tuid.R;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.components.SoporteDialog;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static com.autenticar.tuid.utils.HelperFunctions.CreateAlert;
import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;

public abstract class TuIdActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    Activity activity = this;
    protected DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    protected ImageView burgerIcon;

    public View headerView;

    public Context context = this;
    Toolbar toolbar;
    MediaPlayer mp;


    public Activity main = this;
    protected RelativeLayout fullLayout;
    protected FrameLayout actContent;

    public static boolean playFeDeVidaSound = true;
    public static boolean playRegistrateSound = true;


    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (sound) {
            setSound("stop", activity);
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mp!=null)mp.stop();
    }

    @Override
    public void setContentView(final int layoutResID){

        fullLayout= (RelativeLayout) getLayoutInflater().inflate(R.layout.v2_tuid_activity, null);
        actContent= fullLayout.findViewById(R.id.content_frame);

        getLayoutInflater().inflate(layoutResID, actContent, true);
        super.setContentView(fullLayout);

        // Deja libres la barra de estado (arriba) y la de navegacion del telefono (abajo): la barra superior ya no se
        // superpone con la hora y los botones de abajo no quedan tapados por los botones del sistema.
        fullLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(fullLayout, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public androidx.core.view.WindowInsetsCompat onApplyWindowInsets(View v, androidx.core.view.WindowInsetsCompat insets) {
                androidx.core.graphics.Insets bars = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars() | androidx.core.view.WindowInsetsCompat.Type.displayCutout());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            }
        });
        androidx.core.view.ViewCompat.requestApplyInsets(fullLayout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                if (ajusteMenuLateral != null) ajusteMenuLateral.run();   // recalcula el relleno por las barras del telefono
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ajustarMenuLateralABarras(navigationView);
        // Menu lateral con filas propias (layout nav_drawer_content): cada fila abre su opcion.
        // La opcion Entidades adheridas esta oculta por ahora (ver el comentario en el layout).
        int[] filasMenu = { R.id.nav_contacto, R.id.nav_historial, R.id.nav_certificado,
                R.id.nav_terminos_condiciones, R.id.nav_politicas, R.id.nav_empresa };
        for (int fila : filasMenu) {
            View v = findViewById(fila);
            if (v != null) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        abrirOpcionLateral(view.getId());
                    }
                });
            }
        }
        findViewById(R.id.nav_salir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogo = new AlertDialog.Builder(activity);
                dialogo.setTitle("Alerta");
                dialogo.setMessage("¿ Esta seguro que desea salir de la aplicación ?");
                dialogo.setCancelable(true);
                dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo, int id) {
                        dialogo.cancel();
                    }
                });
                dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo, int id) {
                        finish();
                    }
                });
                //Cambio de color los botones del alert
                AlertDialog alert = dialogo.create();
                alert.show();
                Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                btn_negativo.setTextColor(Color.BLACK);
                Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                btn_positivo.setTextColor(Color.BLACK);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...


        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_on) {
            if (sound) {
                item.setIcon(R.drawable.off_sound_3);
                item.setTitle(R.string.off);
                sound = false;
                if(mp!=null) mp.stop();
            } else {
                item.setIcon(R.drawable.on_sound_3);
                item.setTitle(R.string.on);
                sound = true;
            }
        } else if (id == R.id.help) {
//            HelperFunctions.showDialog(R.string.nav_titulo_contacto, R.string.nav_content_contacto, 4, this, activity);
            new SoporteDialog(this,this,R.string.nav_content_contacto);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        this.menu = menu;
        updateSoundButton();
        return true;
    }
    public Menu menu;

    public void updateSoundButton(){
        if (sound) {
            menu.findItem(R.id.action_on).setIcon(R.drawable.on_sound_3);
            menu.findItem(R.id.action_on).setTitle(R.string.on);
        } else {
            menu.findItem(R.id.action_on).setIcon(R.drawable.off_sound_3);
            menu.findItem(R.id.action_on).setTitle(R.string.off);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** Ajuste del menu lateral a las barras del telefono; se vuelve a correr al abrirlo (ver ajustarMenuLateralABarras). */
    private Runnable ajusteMenuLateral;

    /**
     * Segun la version de Android y el modo de pantalla, el menu lateral puede quedar debajo de la barra de estado o de la de
     * navegacion del telefono (los botones o la barra de gestos tapaban "Salir"). Se mide cuanto se superpone y se agrega ese
     * relleno al encabezado (arriba) y al pie con "Salir" (abajo). Si no hay superposicion no cambia nada.
     */
    private void ajustarMenuLateralABarras(final View menu) {
        final View encabezado = menu.findViewById(R.id.nav_encabezado);
        final View pie = menu.findViewById(R.id.nav_pie);
        if (encabezado == null || pie == null) return;
        final int alturaEncabezado = encabezado.getLayoutParams().height;
        final int rellenoPie = pie.getPaddingBottom();
        final int[] aplicado = { -1, -1 };
        ajusteMenuLateral = new Runnable() {
            @Override
            public void run() {
                androidx.core.view.WindowInsetsCompat insets = androidx.core.view.ViewCompat.getRootWindowInsets(menu);
                if (insets == null) return;
                androidx.core.graphics.Insets barras = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars() | androidx.core.view.WindowInsetsCompat.Type.displayCutout());
                int[] pos = new int[2];
                menu.getLocationInWindow(pos);
                int altoVentana = menu.getRootView().getHeight();
                int arriba = Math.max(0, barras.top - pos[1]);
                int abajo = Math.max(0, (pos[1] + menu.getHeight()) - (altoVentana - barras.bottom));
                if (arriba == aplicado[0] && abajo == aplicado[1]) return;
                aplicado[0] = arriba;
                aplicado[1] = abajo;
                android.view.ViewGroup.LayoutParams lp = encabezado.getLayoutParams();
                lp.height = alturaEncabezado + arriba;
                encabezado.setLayoutParams(lp);
                encabezado.setPadding(0, arriba, 0, 0);
                pie.setPadding(pie.getPaddingLeft(), pie.getPaddingTop(), pie.getPaddingRight(), rellenoPie + abajo);
            }
        };
        menu.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int l, int t, int r, int b, int ol, int ot, int or, int ob) {
                ajusteMenuLateral.run();
            }
        });
        menu.post(ajusteMenuLateral);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return abrirOpcionLateral(menuItem.getItemId());
    }

    /** Abre la opcion elegida en el menu lateral y cierra el menu. */
    protected boolean abrirOpcionLateral(int id) {
        if (id == R.id.nav_terminos_condiciones) {
            new AmplitudeManager().log(this, "Going to Terms", null);
            HelperFunctions.showDialog(R.string.nav_titulo_termino_condiciones, R.string.nav_content_termino_condiciones, 1, this, activity);
        } else if (id == R.id.nav_politicas) {
            new AmplitudeManager().log(this, "Going to Privacy", null);
            HelperFunctions.showDialog(R.string.nav_titulo_politicas, R.string.nav_content_politicas, 2, this, activity);
        } else if (id == R.id.nav_empresa) {
            new AmplitudeManager().log(this, "Going to Company", null);
            HelperFunctions.showDialog(R.string.nav_titulo_empresa, R.string.nav_content_empresa, 3, this, activity);
        } else if (id == R.id.nav_contacto) {
//            HelperFunctions.showDialog(R.string.nav_titulo_contacto, R.string.nav_content_contacto, 4, this, activity);
            new SoporteDialog(this,this,R.string.nav_content_contacto);
        } else if (id == R.id.nav_historial) {
            new AmplitudeManager().log(this, "Going to History", null);
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_entidades_adheridas) {
            new AmplitudeManager().log(this, "Going to Entities", null);
            Intent intent = new Intent(this, EntidadesAdheridasActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_certificado){
            searchDniForCertificate();
        } else if (id == R.id.nav_salir) {
            AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
            dialogo.setTitle("Alerta");
            dialogo.setMessage("¿ Esta seguro que desea salir de la aplicación ?");
            dialogo.setCancelable(true);
            dialogo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    dialogo.cancel();
                }
            });
            dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo, int id) {
                    finish();
                }
            });
            //Cambio de color los botones del alert
            AlertDialog alert = dialogo.create();
            alert.show();
            Button btn_negativo = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            btn_negativo.setTextColor(Color.BLACK);
            Button btn_positivo = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btn_positivo.setTextColor(Color.BLACK);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(menu!=null) updateSoundButton();
    }


    public void playGuideSounds(int[] raws,int startPos, Runnable afterAllRunnable){
        if(startPos==0 && mp!=null) mp.stop();
        
        if(startPos>raws.length-1){
            if(afterAllRunnable!=null) afterAllRunnable.run();
            return;
        }
        if(!sound) {
            playGuideSounds(raws, startPos + 1, afterAllRunnable);
            return;
        }
        mp = MediaPlayer.create(this,raws[startPos]);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playGuideSounds(raws, startPos+1, afterAllRunnable);
            }
        });
        mp.start();
    }


    private Dialog dialog;
    public void searchDniForCertificate(){
        if (com.autenticar.tuid.BuildConfig.USE_CUIL_API) {
            // Por CUIL: se elige la persona (o se ingresa su CUIL) y no se pide DNI ni sexo
            new com.autenticar.tuid.usuarios.SelectorUsuario(this, com.autenticar.tuid.usuarios.LocalUserStore.get(this),
                    "la solicitud de certificado", new com.autenticar.tuid.usuarios.SelectorUsuario.Callback() {
                @Override
                public void onCuil(String cuil) {
                    Intent i = new Intent(context, CertificadoActivity.class);
                    i.putExtra("cuil", cuil);
                    startActivity(i);
                }

                @Override
                public void onRegistrar() {
                    startActivity(new Intent(context, TerminosYCondicionesActivity.class));
                }
            }).mostrar();
            return;
        }
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_autenticar_dni);
        TextView text_title_info_empresarialdialog = (TextView) dialog.findViewById(R.id.text_title_info_empresarial);
        text_title_info_empresarialdialog.setText("Certificado");
        ImageView btn_cancel = (ImageView) dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
        final EditText text_content_info_cuil_text = (EditText) dialog.findViewById(R.id.text_content_info_CUIL_text);
        final RadioGroup rdb_genero = (RadioGroup) dialog.findViewById(R.id.rdb_genero);
//        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.showSoftInput(text_content_info_cuil_text, InputMethodManager.SHOW_IMPLICIT);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dni=String.valueOf(text_content_info_cuil_text.getText());
                String sexo = rdb_genero.getCheckedRadioButtonId()==R.id.rdb_masculino?"M":"F";
                if (dni.trim().equals("")) {
                    text_content_info_cuil_text.requestFocus();
                    HelperFunctions.ShowMensaje(activity, R.drawable.ic_alerta_roja, "Campo requerido", activity.getString(R.string.contact_validar_dni));
                    return;
                }
                Intent i = new Intent(context, CertificadoActivity.class);
                i.putExtra("sexo", sexo);
                i.putExtra("dni",dni);
                startActivity(i);
                dialog.dismiss();

            }

        });
        dialog.show();
    }
}
