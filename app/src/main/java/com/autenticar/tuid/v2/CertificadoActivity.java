package com.autenticar.tuid.v2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.R;
import com.autenticar.tuid.services.TuidService;
import com.autenticar.tuid.services.utils.ServiceGenerator;
import com.autenticar.tuid.utils.HelperFunctions;
import com.autenticar.tuid.v2.services.Certificado;
import com.autenticar.tuid.v2.services.SolicitarCertificadoRequest;
import com.autenticar.tuid.v2.services.SolicitarCertificadoResponse;
import com.autenticar.tuid.v2.utils.FilesUtil;
import com.autenticar.tuid.v2.utils.LoadingDialog;
import com.bumptech.glide.Glide;
import com.izettle.html2bitmap.Html2Bitmap;
import com.izettle.html2bitmap.content.WebViewContent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

//import io.fabric.sdk.android.services.concurrency.AsyncTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CertificadoActivity extends TuIdActivity{

    private static final int PERMISSION_REQUEST_CODE = 12309;
    Activity context = this;
    Certificado cert;
    @Override
    public void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.solicitar_certificado_activity);
        setTitle("Certificado de Fe de vida");

        String dni = getIntent().getStringExtra("dni");
        String sexo = getIntent().getStringExtra("sexo");

        final SolicitarCertificadoRequest request = new SolicitarCertificadoRequest();
        TuidService service = ServiceGenerator.createService(TuidService.class);
        Call<SolicitarCertificadoResponse> callUser = service.getCertificate(dni, sexo);
        LoadingDialog.getInstance().show(this, "Aguarde un momento, procesando la información");
        callUser.enqueue(new Callback<SolicitarCertificadoResponse>() {
            @Override
            public void onResponse(Call<SolicitarCertificadoResponse> call, Response<SolicitarCertificadoResponse> response) {
                LoadingDialog.getInstance().hide();
                SolicitarCertificadoResponse certResponse = response.body();
                if(certResponse.error==0) {
                    cert = response.body().certificado;
                    fillData(certResponse.certificado);
                }
                else{
                    TuidDialog.show(context, "Solicitud de certificado", certResponse.message,response.body().code, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<SolicitarCertificadoResponse> call, Throwable t) {
                LoadingDialog.getInstance().hide();
                HelperFunctions.ShowAlertWithContact(CertificadoActivity.this, context.getString(R.string.no_hay_conexion_a_internet), 1, "0");
                Log.e("ERROR", "FAIL");
                Log.e("ERROR", t.getMessage());

            }

        });
    }



    void fillData(Certificado cert){
        ((TextView)findViewById(R.id.solicitar_certificado_activity_cuil)).setText(cert.cuil);
        ((TextView)findViewById(R.id.solicitar_certificado_activity_fecha_emision)).setText(cert.fecha);
        ((TextView)findViewById(R.id.solicitar_certificado_activity_nombre)).setText(cert.nombre);
        ((TextView)findViewById(R.id.solicitar_certificado_activity_ultima)).setText(cert.ultima);
        String entidades = "";
        for(String s: cert.empresas) entidades += s+" ";
        ((TextView)findViewById(R.id.solicitar_certificado_activity_entidades)).setText(entidades);
        ((TextView)findViewById(R.id.solicitar_certificado_activity_cuil)).setText(cert.cuil);


        String qr = "https://api.qrserver.com/v1/create-qr-code/?size=256x256&data=" + (cert.url.length()>0?cert.url:"NO DATA");
        
        Glide.with(activity).load(qr).into((ImageView) findViewById(R.id.solicitar_certificado_activity_qr));

        try {
            findViewById(R.id.solicitar_certificado_activity_volver).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
        catch (Exception e){

        }

        findViewById(R.id.solicitar_certificado_activity_descargar_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateWeb();
            }
        });
    }


    void generateWeb(){
        LoadingDialog.getInstance().show(this, "Aguarde un momento, procesando la información");
        String html = getHtml(context);
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {

                return new Html2Bitmap.Builder().setContext(context).setContent(WebViewContent.html(html)).build().getBitmap();
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    createPdf(bitmap);
                }
            }
        }.execute();

    }


    void createPdf(Bitmap bitmap){
        PdfDocument myPdfDoc = new PdfDocument();
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page myPage = myPdfDoc.startPage(myPageInfo);
        File myFile = null;
        String exportedPathPdf = "";
        Canvas canvas = myPage.getCanvas();

        canvas.drawBitmap(bitmap,new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()), new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()), null);

        int layoutWidth = 800;
        int pageWidth = 800;
        int pageHeight = 600;
        // Find the tallest section

        // Computed values


        myPdfDoc.finishPage(myPage);

        try {
            myFile = FilesUtil.createFile(activity, ".pdf");
            myPdfDoc.writeTo(new FileOutputStream(myFile));
        }catch (Exception e){
            e.printStackTrace();
        }
        myPdfDoc.close();

        if(myFile != null) {
            exportedPathPdf = myFile.getAbsolutePath();
            Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setType("text/plain");

            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(FilesUtil.getUriFromFile(context.getBaseContext(),myFile));


            Intent pdfIntent = new Intent();
            pdfIntent.setType("application/pdf");
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfIntent.setAction(Intent.ACTION_VIEW);
            Uri uri = uris.get(0);
            pdfIntent.setDataAndType(uri, "application/pdf");
            context.startActivity(pdfIntent);
        }
        LoadingDialog.getInstance().hide();
    }



    String getHtml(Context context){
        InputStream is = context.getResources().openRawResource(R.raw.certificate);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }
        catch (Exception e){
            Log.e("EX", e.getMessage());
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("EX2", e.getMessage());
            }
        }

        String html = writer.toString();
        if(cert!=null) {
            String entidades = "";
            for(String s: cert.empresas) entidades += s+" ";
            String qr = "https://api.qrserver.com/v1/create-qr-code/?size=256x256&data=" + (cert.url.length()>0?cert.url:"NO DATA");
            qr = "<img alt=\"Client Logo\" title=\"Client Logo\" src=\""+ qr +"\" /> ";

            html = html.replaceAll("%cuil%", cert.cuil)
                    .replaceAll("%nombre%", cert.nombre)
                    .replaceAll("%fecha%", cert.fecha)
                    .replaceAll("%validez%",cert.validez)
                    .replaceAll("%ultima%", cert.ultima)
                    .replace("%qr%", cert.url.length()>0?qr:"")
                    .replaceAll("%nacimiento%", cert.nacimiento)
                    .replaceAll("%empresas%", entidades);
        }
        return html;
    }


}
