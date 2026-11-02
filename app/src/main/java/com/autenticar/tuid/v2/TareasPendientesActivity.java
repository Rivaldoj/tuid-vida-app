package com.autenticar.tuid.v2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.autenticar.tuid.BuildConfig;
import com.autenticar.tuid.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class TareasPendientesActivity extends AppCompatActivity {

    List<JSONObject> forms = new ArrayList<>();
    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.pending_tasks_activity);
        getSupportActionBar().show();
        getSupportActionBar().setTitle("Tareas Pendientes");
        try{
            String token = getIntent().getStringExtra("token");
            JSONArray pendingTasksArr = new JSONArray(getIntent().getStringExtra("pendingTasks"));
            if(pendingTasksArr.length()==0) finish();
            for(int i = 0; i<pendingTasksArr.length(); i++){
                try{
                    JSONObject pending_task = pendingTasksArr.getJSONObject(i);
//                    Log.e("PT", pending_task.toString(2));
                    if(pending_task.has("versionMinima") && pending_task.getInt("versionMinima")<= BuildConfig.VERSION_CODE) forms.add(pending_task);
                }
                catch (Exception e){

                }
            }
            initlList(token);

        }
        catch (Exception e){
            Log.e("EX",e.getMessage());
            finish();
        }
    }

    void initlList(String token){
        ListView lv = findViewById(R.id.pending_tasks_list);
        List<String> titles = new ArrayList<>();
        for(JSONObject form : forms)  titles.add(form.optString("nombre", " "));
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,titles.toArray(new String[forms.size()]) ));

        findViewById(R.id.pending_tasks_comenzar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(JSONObject form: forms){
                    Intent intent = new Intent(HomeActivity.self, FormActivity.class);
//                        Bundle b = new Bundle();
//                        b.putSerializable("form", form.ser);
                    intent.putExtra("form", form.toString());
                    intent.putExtra("token", token);
                    startActivity(intent);

                }
                finish();
            }
        });

    }
}
