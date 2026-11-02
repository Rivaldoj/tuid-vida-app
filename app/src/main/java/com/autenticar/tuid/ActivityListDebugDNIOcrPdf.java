package com.autenticar.tuid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.autenticar.tuid.three.DNIBack_OCR;
import com.autenticar.tuid.three.DNIBack_PDF;
import com.autenticar.tuid.three.DNIFront_OCR;
import com.autenticar.tuid.three.DNIFront_PDF;
import com.autenticar.tuid.utils.SQLHelper;

import java.util.ArrayList;

public class ActivityListDebugDNIOcrPdf extends AppCompatActivity {
    private ListView list_DNI;
    public ArrayList<DNIFront_OCR> item_DNIFront_OCR;
    public ArrayList<DNIFront_PDF> item_DNIFront_PDF;
    public ArrayList<DNIBack_OCR> item_DNIBack_OCR;
    public ArrayList<DNIBack_PDF> item_DNIBack_PDF;
    private static AdapterListDebugDNIOcrPdf adapterListDebugDNIOcrPdf;
    final Activity activity = this;
    private String TAG="ActivityListDebugDNIOcrPdf";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_cuil);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Intent intent=getIntent();
        Bundle args = intent.getBundleExtra("front_ocr");
        //System.out.println("ARG ");
        item_DNIFront_OCR = (ArrayList<DNIFront_OCR>) args.getSerializable("front_ocr_array");
        item_DNIFront_PDF=new ArrayList<>();
        item_DNIBack_OCR=new ArrayList<>();
        item_DNIBack_PDF=new ArrayList<>();
        list_DNI = findViewById(R.id.list_cuil);
       // sql = new SQLHelper(this);
        //item = sql.getAllCUIL();
        // item.listIterator().();


        if(item_DNIFront_OCR.size()>0){
            adapterListDebugDNIOcrPdf = new AdapterListDebugDNIOcrPdf(this, item_DNIFront_OCR, null, null, null);
        }else if(item_DNIFront_PDF.size()>0){
            adapterListDebugDNIOcrPdf = new AdapterListDebugDNIOcrPdf(this, null, item_DNIFront_PDF, null, null);
        }else if(item_DNIBack_OCR.size()>0){
            adapterListDebugDNIOcrPdf = new AdapterListDebugDNIOcrPdf(this, null, null, item_DNIBack_OCR, null);
        }else if(item_DNIBack_PDF.size()>0){
            adapterListDebugDNIOcrPdf = new AdapterListDebugDNIOcrPdf(this, null, null, null, item_DNIBack_PDF);
        }


        list_DNI.setAdapter(adapterListDebugDNIOcrPdf);
        if (adapterListDebugDNIOcrPdf.getCount() == 0) {
            findViewById(R.id.no_registro).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_registro).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
