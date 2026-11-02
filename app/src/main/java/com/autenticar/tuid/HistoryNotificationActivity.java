package com.autenticar.tuid;

import android.app.Activity;
import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.view.ViewPager;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.autenticar.tuid.db.NotificationDBHelper;
import com.autenticar.tuid.model.Notification;
import com.autenticar.tuid.utils.SQLHelper;

import java.util.ArrayList;

import static com.autenticar.tuid.utils.HelperFunctions.setSound;
import static com.autenticar.tuid.utils.HelperFunctions.sound;

public class HistoryNotificationActivity  extends AppCompatActivity {
    private ListView list;
    public ArrayList<Notification> item;
    private ViewPager mViewPager;
    private static HistoryNotificationAdapter historyAdapter;
    final Activity activity = this;
    private SQLHelper sql;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        list=(ListView) findViewById(R.id.list_notification);
        sql=new SQLHelper(activity);
        sql.dropNotification();
        sql.insertNotification(new Notification("hola mundo", "auto mensaje de respuesta", "18/07/2019"));
        item=sql.getAllNotification(getIntent().getStringExtra("Token"));
        // item.listIterator().();
        historyAdapter = new HistoryNotificationAdapter(activity, item);
        list.setAdapter(historyAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
